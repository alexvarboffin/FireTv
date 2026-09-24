package tv.hdonlinetv.compose.util

import android.content.Context
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistRefreshResult

object PlaylistRefreshFeedback {
    fun message(context: Context, result: PlaylistRefreshResult): String = when (result) {
        is PlaylistRefreshResult.Success -> {
            val stats = context.getString(
                R.string.refresh_channels_stats,
                result.channelCount,
                result.previousCount,
            )
            "${context.getString(R.string.download_successful)}\n$stats"
        }
        PlaylistRefreshResult.Empty -> context.getString(R.string.no_lines_found)
        PlaylistRefreshResult.Failed -> context.getString(R.string.error_download_failed)
    }

    fun isSuccess(result: PlaylistRefreshResult): Boolean =
        result is PlaylistRefreshResult.Success
}
