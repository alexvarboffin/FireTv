package tv.hdonlinetv.compose.core.epg

import java.text.Normalizer
import java.util.Locale
import java.util.zip.CRC32

/**
 * Matching keys. Must stay identical to `epgkeys.py` in the epg-index repo
 * (see Documentation/ARCHITECTURE/epg.md) — the index is keyed by these values.
 */
object EpgKey {

    /**
     * Key for `tvg-id` (M3U), `epg_channel_id` (Xtream) and XMLTV `channel` ids.
     * Case, punctuation and `@feed` suffixes differ between sources (`3CatInfo.es` vs `3catinfo.es@HD`).
     */
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

    /** `Canal Sur Andalucía (1080p) [Geo-blocked]` → `canalsurandalucia`; non-Latin names → null. */
    fun nameKey(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val folded = COMBINING.replace(Normalizer.normalize(raw, Normalizer.Form.NFKD), "")
            .lowercase(Locale.ROOT)
        val text = QUALITY.replace(BRACKETS.replace(folded, " "), " ")
        val key = NON_ALNUM.replace(text, "")
        return key.ifEmpty { null }
    }

    /** CRC-32 of the UTF-8 key modulo [shards] (`zlib.crc32` on the server). */
    fun shardOf(key: String, shards: Int): Int {
        val crc = CRC32().apply { update(key.toByteArray(Charsets.UTF_8)) }.value
        return (crc % shards).toInt()
    }

    private val COMBINING = Regex("\\p{Mn}+")
    private val BRACKETS = Regex("\\([^)]*\\)|\\[[^\\]]*]")
    /** Python's Unicode `\b`/`\d`, spelled out: Android ICU rejects `(?U)`, plain JVM `\b` is ASCII-only. */
    private val QUALITY = Regex(
        "(?<![\\p{L}\\p{N}_])(?:hd|fhd|uhd|sd|4k|8k|hevc|h\\.?26[45]|\\p{Nd}{3,4}[pi])(?![\\p{L}\\p{N}_])",
    )
    private val NON_ALNUM = Regex("[^0-9a-z]+")
}
