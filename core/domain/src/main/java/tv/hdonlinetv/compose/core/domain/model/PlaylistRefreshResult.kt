package tv.hdonlinetv.compose.core.domain.model

sealed class PlaylistRefreshResult {
    data class Success(
        val channelCount: Int,
        val previousCount: Int,
    ) : PlaylistRefreshResult()

    data object Empty : PlaylistRefreshResult()
    data object Failed : PlaylistRefreshResult()
}
