package tv.hdonlinetv.compose.core.databridge.channel

import android.content.Context
import com.walhalla.data.repository.LocalDatabaseRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.repository.ChannelRepository

class LocalChannelRepository(
    context: Context,
) : ChannelRepository {

    private val database = LocalDatabaseRepo.getStoreInfoDatabase(context.applicationContext)

    override suspend fun getAllChannels(sortOption: Int): List<ChannelUi> = withContext(Dispatchers.IO) {
        ChannelMapper.toUiList(database.getAllChannels(sortOption))
    }

    override fun observeAllChannels(sortOption: Int): Flow<List<ChannelUi>> =
        database.observeAllChannels(sortOption)
            .map { ChannelMapper.toUiList(it) }
            .flowOn(Dispatchers.IO)

    override suspend fun getChannelsInCategory(categoryName: String): List<ChannelUi> = withContext(Dispatchers.IO) {
        ChannelMapper.toUiList(database.getCategory(categoryName))
    }

    override suspend fun getChannelsInPlaylist(playlistId: Long, sortOption: Int): List<ChannelUi> =
        withContext(Dispatchers.IO) {
            ChannelMapper.toUiList(database.getChannelsInPlaylist(playlistId, sortOption))
        }

    override suspend fun getPlaylistIdForChannel(channelId: Long): Long? = withContext(Dispatchers.IO) {
        database.getPlaylistIdForChannel(channelId)
    }

    override suspend fun getSiblingChannels(channelId: Long, sortOption: Int): List<ChannelUi> =
        withContext(Dispatchers.IO) {
            val playlistId = database.getPlaylistIdForChannel(channelId)
            if (playlistId != null && playlistId > 0) {
                return@withContext ChannelMapper.toUiList(
                    database.getChannelsInPlaylist(playlistId, sortOption),
                )
            }
            val channel = try {
                database.getChannelById(channelId)
            } catch (_: Exception) {
                null
            }
            val cat = channel?.cat
            if (!cat.isNullOrBlank()) {
                ChannelMapper.toUiList(database.getCategory(cat))
            } else {
                emptyList()
            }
        }

    override suspend fun getChannelById(id: Long): ChannelUi? = withContext(Dispatchers.IO) {
        try {
            ChannelMapper.toUi(database.getChannelById(id))
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getFavorites(playlistId: Long): List<ChannelUi> = withContext(Dispatchers.IO) {
        ChannelMapper.toUiList(database.getFavorite(playlistId))
    }

    override fun observeFavorites(playlistId: Long): Flow<List<ChannelUi>> =
        database.observeFavorites(playlistId)
            .map { ChannelMapper.toUiList(it) }
            .flowOn(Dispatchers.IO)

    override suspend fun search(query: String): List<ChannelUi> = withContext(Dispatchers.IO) {
        ChannelMapper.toUiList(database.searchChannel(query))
    }

    override suspend fun isFavorite(channelId: Long): Boolean = withContext(Dispatchers.IO) {
        database.isFavorite(channelId) == 1
    }

    override suspend fun setFavorite(channelId: Long, favorite: Boolean) {
        withContext(Dispatchers.IO) {
            val channel = database.getChannelById(channelId)
            if (favorite) {
                database.addFavorite(channel)
            } else {
                database.deleteFavorite(channel)
            }
        }
    }
}
