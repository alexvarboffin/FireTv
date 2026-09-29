package tv.hdonlinetv.compose.core.epg

import java.util.Locale

/**
 * Matching key for `tvg-id` (M3U), `epg_channel_id` (Xtream) and XMLTV `channel` ids.
 * Case, punctuation and `@feed` suffixes differ between sources (`3CatInfo.es` vs `3catinfo.es@HD`).
 */
object EpgKey {
    fun normalize(raw: String?): String? {
        val base = raw?.substringBefore('@')?.trim().orEmpty()
        if (base.isEmpty()) return null
        val key = buildString(base.length) {
            for (ch in base.lowercase(Locale.ROOT)) {
                if (ch.isLetterOrDigit()) append(ch)
            }
        }
        return key.ifEmpty { null }
    }
}
