package tv.hdonlinetv.compose.core.epg.xmltv

import java.util.GregorianCalendar
import java.util.TimeZone

/** XMLTV timestamps: `yyyyMMddHHmm[ss] [+-]HHMM`; no offset means UTC. */
object XmlTvTime {
    private val pattern = Regex("""^(\d{4})(\d{2})(\d{2})(\d{2})(\d{2})(\d{2})?\s*([+-])?(\d{2})?:?(\d{2})?""")
    private val utc: TimeZone = TimeZone.getTimeZone("UTC")

    /** Epoch millis (UTC) or null when unparseable. */
    fun parse(raw: String?): Long? {
        val m = pattern.find(raw?.trim().orEmpty()) ?: return null
        val g = m.groupValues
        val cal = GregorianCalendar(utc).apply {
            clear()
            set(g[1].toInt(), g[2].toInt() - 1, g[3].toInt(), g[4].toInt(), g[5].toInt(), g[6].ifEmpty { "0" }.toInt())
        }
        val local = cal.timeInMillis
        if (g[7].isEmpty() || g[8].isEmpty()) return local
        val offsetMin = g[8].toInt() * 60 + g[9].ifEmpty { "0" }.toInt()
        val sign = if (g[7] == "-") -1 else 1
        return local - sign * offsetMin * 60_000L
    }
}
