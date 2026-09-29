package tv.hdonlinetv.compose.core.epg.xmltv

import org.junit.Assume.assumeTrue
import org.junit.Test
import org.kxml2.io.KXmlParser
import tv.hdonlinetv.compose.core.epg.EpgKey
import java.io.File

/**
 * Opt-in check on a real downloaded guide:
 * `EPG_LIVE_XML=path/guide.xml[.gz]` and optionally `EPG_LIVE_M3U=path/playlist.m3u`.
 */
class XmlTvLiveFileTest {

    @Test
    fun parseRealGuide() {
        val xmlPath = System.getenv("EPG_LIVE_XML")
        assumeTrue("EPG_LIVE_XML not set", !xmlPath.isNullOrBlank())
        val m3uIds = System.getenv("EPG_LIVE_M3U")?.takeIf { it.isNotBlank() }?.let { path ->
            Regex("tvg-id=\"([^\"]*)\"").findAll(File(path).readText())
                .map { it.groupValues[1] }
                .filter { it.isNotBlank() }
                .toList()
        }
        val wanted = m3uIds?.mapNotNullTo(HashSet()) { EpgKey.normalize(it) }

        val guideKeys = HashSet<String>()
        val parser = XmlTvParser { KXmlParser() }
        val startedAt = System.nanoTime()
        val stats = File(xmlPath).inputStream().use { input ->
            XmlTvStreams.reader(input).use { reader ->
                parser.parse(reader, wanted, Long.MIN_VALUE / 4, Long.MAX_VALUE / 4) {
                    guideKeys += it.channelKey
                }
            }
        }
        val ms = (System.nanoTime() - startedAt) / 1_000_000
        println("EPG live: $stats in ${ms}ms, channels with programmes=${guideKeys.size}")
        if (m3uIds != null && wanted != null) {
            val matched = wanted.count { it in guideKeys }
            println("EPG live: playlist tvg-id total=${m3uIds.size} unique=${wanted.size} matched=$matched")
            println("EPG live: matched sample=" + wanted.filter { it in guideKeys }.take(15))
            println("EPG live: unmatched sample=" + wanted.filter { it !in guideKeys }.take(15))
        }
    }
}
