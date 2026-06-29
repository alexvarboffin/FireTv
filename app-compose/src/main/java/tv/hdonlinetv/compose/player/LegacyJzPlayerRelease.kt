package tv.hdonlinetv.compose.player

import cn.jzvd.Jzvd

object LegacyJzPlayerRelease {

    fun stopAndRelease(player: JZVideoPlayerNew? = null) {
        if (player?.isPlayerReleased == true && Jzvd.CURRENT_JZVD == null) {
            return
        }

        try {
            player?.mediaInterface?.let { media ->
                try {
                    media.pause()
                } catch (_: Exception) {
                }
            }
        } catch (_: Exception) {
        }

        // releaseAllVideos() already calls reset() on CURRENT_JZVD — do not reset twice
        try {
            Jzvd.releaseAllVideos()
        } catch (_: Exception) {
        }

        val orphan = player?.takeIf { it !== Jzvd.CURRENT_JZVD && !it.isPlayerReleased }
        try {
            orphan?.reset()
        } catch (_: Exception) {
        }
    }
}
