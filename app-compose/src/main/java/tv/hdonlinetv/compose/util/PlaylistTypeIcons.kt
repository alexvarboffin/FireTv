package tv.hdonlinetv.compose.util

import androidx.annotation.DrawableRes
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.PlaylistType

object PlaylistTypeIcons {
    @DrawableRes
    fun iconRes(type: Int): Int = when (type) {
        PlaylistType.M3U_CLOUD -> R.drawable.ic_cloud
        PlaylistType.M3U_LOCAL -> R.drawable.ic_local
        PlaylistType.M3U_BUFFER -> R.drawable.ic_buffer
        PlaylistType.XTREAM_URL -> R.drawable.ic_xtream
        else -> R.drawable.ic_playlist
    }
}
