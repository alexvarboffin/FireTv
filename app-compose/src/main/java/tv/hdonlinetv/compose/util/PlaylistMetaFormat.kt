package tv.hdonlinetv.compose.util

import tv.hdonlinetv.compose.core.domain.model.PlaylistType
import tv.hdonlinetv.compose.core.domain.model.PlaylistUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Compact playlist meta line (Cinema/legacy parity):
 * `128 ch · 21 Sep 2026, 17:40 · upd 22 Sep 2026, 10:05`
 * Skips update when unset or equal to import; skips count for Xtream.
 */
object PlaylistMetaFormat {

    private val dateFormat: SimpleDateFormat
        get() = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault()).apply {
            timeZone = TimeZone.getDefault()
        }

    fun formatDate(millis: Long): String {
        if (millis <= 0L) return ""
        return dateFormat.format(Date(millis))
    }

    fun buildMeta(
        playlist: PlaylistUi,
        channelsLabel: (Int) -> String,
        updatedLabel: (String) -> String,
    ): String {
        val parts = ArrayList<String>(3)
        if (playlist.type != PlaylistType.XTREAM_URL) {
            parts += channelsLabel(playlist.count)
        }
        val added = formatDate(playlist.importDate)
        if (added.isNotEmpty()) {
            parts += added
        }
        val updated = formatDate(playlist.updateDate)
        if (updated.isNotEmpty() &&
            playlist.updateDate > 0L &&
            playlist.updateDate != playlist.importDate
        ) {
            parts += updatedLabel(updated)
        }
        return parts.joinToString("  ·  ")
    }
}
