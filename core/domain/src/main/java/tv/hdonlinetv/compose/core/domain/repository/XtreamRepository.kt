package tv.hdonlinetv.compose.core.domain.repository

import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.model.SeriesDetailUi

enum class XtreamStreamType {
    LIVE,
    VOD,
    SERIES,
}

interface XtreamRepository {
    suspend fun getStreams(
        playlistApiUrl: String,
        type: XtreamStreamType,
    ): List<ChannelUi>

    suspend fun getSeriesInfo(
        playlistApiUrl: String,
        seriesId: Int,
    ): SeriesDetailUi?

    fun buildEpisodeStreamUrl(
        playlistApiUrl: String,
        episodeId: String,
        containerExtension: String,
    ): String?
}
