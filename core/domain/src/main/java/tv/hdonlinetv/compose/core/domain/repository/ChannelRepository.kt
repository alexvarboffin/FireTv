package tv.hdonlinetv.compose.core.domain.repository

import kotlinx.coroutines.flow.Flow
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

interface ChannelRepository {
    suspend fun getAllChannels(sortOption: Int = 0): List<ChannelUi>
    fun observeAllChannels(sortOption: Int = 0): Flow<List<ChannelUi>>
    suspend fun getChannelsInCategory(categoryName: String): List<ChannelUi>
    suspend fun getChannelsInPlaylist(playlistId: Long, sortOption: Int = 0): List<ChannelUi>
    suspend fun getChannelById(id: Long): ChannelUi?
    suspend fun getFavorites(playlistId: Long = -1): List<ChannelUi>
    fun observeFavorites(playlistId: Long = -1): Flow<List<ChannelUi>>
    suspend fun search(query: String): List<ChannelUi>
    suspend fun isFavorite(channelId: Long): Boolean
    suspend fun setFavorite(channelId: Long, favorite: Boolean)
}
