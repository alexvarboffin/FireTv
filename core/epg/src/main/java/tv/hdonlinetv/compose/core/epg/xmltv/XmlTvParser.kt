package tv.hdonlinetv.compose.core.epg.xmltv

import org.xmlpull.v1.XmlPullParser
import tv.hdonlinetv.compose.core.epg.EpgKey
import java.io.Reader

data class XmlTvProgramme(
    /** [EpgKey.normalize]d XMLTV `channel` attribute. */
    val channelKey: String,
    val start: Long,
    val stop: Long,
    val title: String,
    val desc: String?,
)

data class XmlTvStats(
    val channels: Int,
    val programmesSeen: Int,
    val programmesKept: Int,
)

/**
 * Streaming XMLTV reader: never holds the whole guide in memory, emits only programmes of
 * [wantedKeys] (all when null) overlapping `[windowStart, windowEnd)`.
 */
class XmlTvParser(
    private val newParser: () -> XmlPullParser,
) {

    fun parse(
        reader: Reader,
        wantedKeys: Set<String>?,
        windowStart: Long,
        windowEnd: Long,
        onProgramme: (XmlTvProgramme) -> Unit,
    ): XmlTvStats {
        val p = newParser()
        p.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        p.setInput(reader)
        var channels = 0
        var seen = 0
        var kept = 0
        var event = p.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG) {
                when (p.name) {
                    "channel" -> {
                        channels++
                        skipElement(p)
                    }
                    "programme" -> {
                        seen++
                        val programme = readProgramme(p, wantedKeys, windowStart, windowEnd)
                        if (programme != null) {
                            kept++
                            onProgramme(programme)
                        }
                    }
                }
            }
            event = p.next()
        }
        return XmlTvStats(channels = channels, programmesSeen = seen, programmesKept = kept)
    }

    /** Leaves the parser on the programme END_TAG. */
    private fun readProgramme(
        p: XmlPullParser,
        wantedKeys: Set<String>?,
        windowStart: Long,
        windowEnd: Long,
    ): XmlTvProgramme? {
        val key = EpgKey.normalize(p.getAttributeValue(null, "channel"))
        val start = XmlTvTime.parse(p.getAttributeValue(null, "start"))
        if (key == null || start == null || (wantedKeys != null && key !in wantedKeys)) {
            skipElement(p)
            return null
        }
        val stop = XmlTvTime.parse(p.getAttributeValue(null, "stop"))
            ?.takeIf { it > start }
            ?: (start + DEFAULT_DURATION_MS)
        if (stop <= windowStart || start >= windowEnd) {
            skipElement(p)
            return null
        }
        var title: String? = null
        var desc: String? = null
        val depth = p.depth
        var event = p.next()
        while (!(event == XmlPullParser.END_TAG && p.depth == depth)) {
            if (event == XmlPullParser.END_DOCUMENT) break
            if (event == XmlPullParser.START_TAG && p.depth == depth + 1) {
                when (p.name) {
                    "title" -> if (title == null) title = readText(p) else skipElement(p)
                    "desc" -> if (desc == null) desc = readText(p) else skipElement(p)
                    else -> skipElement(p)
                }
            }
            event = p.next()
        }
        val safeTitle = title?.trim().orEmpty()
        if (safeTitle.isEmpty()) return null
        return XmlTvProgramme(
            channelKey = key,
            start = start,
            stop = stop,
            title = safeTitle,
            desc = desc?.trim()?.takeIf { it.isNotEmpty() },
        )
    }

    /** Text of the current element; leaves the parser on its END_TAG. */
    private fun readText(p: XmlPullParser): String {
        val sb = StringBuilder()
        val depth = p.depth
        var event = p.next()
        while (!(event == XmlPullParser.END_TAG && p.depth == depth)) {
            if (event == XmlPullParser.END_DOCUMENT) break
            if (event == XmlPullParser.TEXT) sb.append(p.text)
            event = p.next()
        }
        return sb.toString()
    }

    /** From START_TAG to its matching END_TAG. */
    private fun skipElement(p: XmlPullParser) {
        var open = 1
        while (open > 0) {
            when (p.next()) {
                XmlPullParser.START_TAG -> open++
                XmlPullParser.END_TAG -> open--
                XmlPullParser.END_DOCUMENT -> return
            }
        }
    }

    private companion object {
        const val DEFAULT_DURATION_MS = 60 * 60_000L
    }
}
