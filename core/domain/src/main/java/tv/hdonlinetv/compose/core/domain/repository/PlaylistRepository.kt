package tv.hdonlinetv.compose.core.domain.repository

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import tv.hdonlinetv.compose.core.domain.model.PlaylistRefreshResult
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi

interface PlaylistRepository {
    suspend fun getAllPlaylists(): List<PlaylistUi>
    fun observeAllPlaylists(): Flow<List<PlaylistUi>>
    suspend fun getPlaylistById(playlistId: Long): PlaylistUi?
    suspend fun deletePlaylist(playlistId: Long): Int
    suspend fun refreshFromUrl(playlist: PlaylistUi): PlaylistRefreshResult
    suspend fun addFromUrl(title: String, url: String): Long?
    suspend fun addFromFile(title: String, uri: Uri): Long?
    suspend fun addFromClipboardM3u(content: String): Long?
    suspend fun addXtream(
        title: String,
        serverUrl: String,
        username: String,
        password: String,
    ): Long?
}
