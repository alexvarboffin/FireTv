package tv.hdonlinetv.compose.core.databridge.playlist

import android.content.Context
import android.net.Uri
import com.walhalla.data.model.Channel
import com.walhalla.data.model.PlaylistImpl
import com.walhalla.data.repository.LocalDatabaseRepo
import com.walhalla.data.repository.M3UParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistRefreshResult
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import tv.hdonlinetv.compose.core.domain.repository.PlaylistRepository
import tv.hdonlinetv.compose.core.epg.EpgStore
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import java.io.BufferedReader
import java.io.InputStreamReader

class LocalPlaylistRepository(
    context: Context,
) : PlaylistRepository {

    private val appContext = context.applicationContext
    private val database = LocalDatabaseRepo.getStoreInfoDatabase(appContext)
    private val settingsRepository = LocalSettingsRepository(appContext)
    private val httpClient = OkHttpClient()
    private val epgStore = EpgStore.get(appContext)

    override suspend fun getAllPlaylists(): List<PlaylistUi> = withContext(Dispatchers.IO) {
        PlaylistMapper.toUiList(database.selectAllPlaylist())
    }

    override fun observeAllPlaylists(): Flow<List<PlaylistUi>> =
        database.observeAllPlaylists()
            .map { PlaylistMapper.toUiList(it) }
            .flowOn(Dispatchers.IO)

    override suspend fun getPlaylistById(playlistId: Long): PlaylistUi? = withContext(Dispatchers.IO) {
        database.selectAllPlaylist()
            .firstOrNull { it._id == playlistId }
            ?.let { PlaylistMapper.toUi(it) }
    }

    override suspend fun deletePlaylist(playlistId: Long): Int = withContext(Dispatchers.IO) {
        val playlist = database.selectAllPlaylist().firstOrNull { it._id == playlistId }
            ?: return@withContext 0
        val cleanupEmptyCategories = settingsRepository.getSettings().cleanupEmptyCategories
        val deleted = database.deletePlaylistAndRelatedChannels(playlist, cleanupEmptyCategories)
        runCatching { epgStore.unbindPlaylist(playlistId) }
        deleted
    }

    override suspend fun refreshFromUrl(playlist: PlaylistUi): PlaylistRefreshResult = withContext(Dispatchers.IO) {
        try {
            val body = downloadText(playlist.fileName)
                ?: return@withContext PlaylistRefreshResult.Failed
            val channels = M3UParser.parseM3U(appContext, body)
            if (channels.isEmpty()) {
                return@withContext PlaylistRefreshResult.Empty
            }
            val previousCount = playlist.count
            // Keep playlist row (same _id) so UI / navigation are not wiped mid-refresh.
            database.clearPlaylistChannelLinks(playlist.id)
            val refreshed = PlaylistMapper.toData(playlist)
            refreshed.updateDate = System.currentTimeMillis()
            refreshed.count = channels.size
            database.addChannelAndPlaylist(channels, refreshed)
            bindGuides(playlist.id, body, channels)
            PlaylistRefreshResult.Success(
                channelCount = channels.size,
                previousCount = previousCount,
            )
        } catch (_: Exception) {
            PlaylistRefreshResult.Failed
        }
    }

    override suspend fun addFromUrl(title: String, url: String): Long? = withContext(Dispatchers.IO) {
        try {
            val resolvedTitle = title.ifBlank {
                PlaylistUrlHelper.deriveTitleFromUrl(url)
            }
            val body = downloadText(url) ?: return@withContext null
            val channels = M3UParser.parseM3U(appContext, body)
            if (channels.isEmpty()) return@withContext null
            val playlist = PlaylistMapper.newCloudPlaylist(resolvedTitle, url)
            playlist.updateDate = System.currentTimeMillis()
            database.addChannelAndPlaylist(channels, playlist)
            database.selectAllPlaylist().lastOrNull()?._id
                ?.also { bindGuides(it, body, channels) }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun addFromFile(title: String, uri: Uri): Long? = withContext(Dispatchers.IO) {
        try {
            val inputStream = appContext.contentResolver.openInputStream(uri) ?: return@withContext null
            val content = inputStream.bufferedReader().use(BufferedReader::readText)
            val channels = M3UParser.parseM3U(appContext, content)
            if (channels.isEmpty()) return@withContext null
            val resolvedTitle = title.ifBlank {
                uri.lastPathSegment?.substringAfterLast('/')?.substringBeforeLast('.').orEmpty()
            }
            val path = uri.path.orEmpty()
            val playlist = PlaylistImpl(
                resolvedTitle,
                path,
                System.currentTimeMillis(),
                -1,
                true,
                PlaylistType.M3U_LOCAL,
            )
            database.addChannelAndPlaylist(channels, playlist)
            database.selectAllPlaylist().lastOrNull()?._id
                ?.also { bindGuides(it, content, channels) }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun addFromClipboardM3u(content: String): Long? = withContext(Dispatchers.IO) {
        try {
            val channels = M3UParser.parseM3U(appContext, content)
            if (channels.isEmpty()) return@withContext null
            val title = content.take(15).ifBlank { "Clipboard" }
            val playlist = PlaylistImpl(
                title,
                "",
                System.currentTimeMillis(),
                0,
                false,
                PlaylistType.M3U_BUFFER,
            )
            database.addChannelAndPlaylist(channels, playlist)
            database.selectAllPlaylist().lastOrNull()?._id
                ?.also { bindGuides(it, content, channels) }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun addXtream(
        title: String,
        serverUrl: String,
        username: String,
        password: String,
    ): Long? = withContext(Dispatchers.IO) {
        try {
            val apiUrl = PlaylistUrlHelper.buildPlayerApiUrl(serverUrl, username, password)
            val request = Request.Builder().url(apiUrl).build()
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null
            val resolvedTitle = title.ifBlank {
                val host = apiUrl.toHttpUrlOrNull()?.host
                PlaylistUrlHelper.deriveTitleFromUrl(serverUrl, host)
            }
            val playlist = PlaylistImpl(
                resolvedTitle,
                apiUrl,
                System.currentTimeMillis(),
                -1,
                false,
                PlaylistType.XTREAM_URL,
            )
            database.addXtreamPlaylist(playlist).takeIf { it > 0 }
        } catch (_: Exception) {
            null
        }
    }

    private fun downloadText(url: String): String? {
        val request = Request.Builder().url(url).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            return response.body?.string()
        }
    }

    /** Playlist's own `url-tvg` guides; a failure here must never fail the import. */
    private suspend fun bindGuides(playlistId: Long, body: String, channels: List<Channel>) {
        runCatching {
            epgStore.bindPlaylistGuides(
                playlistId = playlistId,
                urls = M3UParser.parseHeaderEpgUrls(body),
                tvgIds = channels.map { it.tvgId },
            )
        }
    }
}
