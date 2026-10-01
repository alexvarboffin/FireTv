package tv.hdonlinetv.compose.core.epg.xmltv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.kxml2.io.KXmlParser
import tv.hdonlinetv.compose.core.epg.EpgKey
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPOutputStream

class XmlTvParserTest {

    private val parser = XmlTvParser { KXmlParser() }
    private val t0 = XmlTvTime.parse("20260928100000 +0000")!!
    private val hour = 3_600_000L

    private val xml = """
        <?xml version="1.0" encoding="UTF-8"?>
        <!DOCTYPE tv SYSTEM "xmltv.dtd">
        <tv generator-info-name="test">
          <channel id="3CatInfo.es"><display-name>3CatInfo</display-name><icon src="http://x/i.png"/></channel>
          <channel id="Other.es"><display-name>Other</display-name></channel>
          <programme start="20260928090000 +0000" stop="20260928100000 +0000" channel="3CatInfo.es">
            <title lang="es">Ended</title>
          </programme>
          <programme start="20260928100000 +0000" stop="20260928110000 +0000" channel="3catinfo.es@HD">
            <title lang="es">Noticias &amp; más</title>
            <title lang="ca">Notícies</title>
            <desc lang="es">Informativo</desc>
            <category>News</category>
          </programme>
          <programme start="20260928110000 +0000" channel="3CatInfo.es">
            <title>No stop</title>
          </programme>
          <programme start="20260928100000 +0000" stop="20260928110000 +0000" channel="Other.es">
            <title>Not wanted</title>
          </programme>
          <programme start="20260930100000 +0000" stop="20260930110000 +0000" channel="3CatInfo.es">
            <title>Too far</title>
          </programme>
          <programme start="bad" stop="20260928110000 +0000" channel="3CatInfo.es"><title>Bad time</title></programme>
          <programme start="20260928120000 +0000" stop="20260928130000 +0000" channel="3CatInfo.es"><title> </title></programme>
        </tv>
    """.trimIndent()

    private fun run(input: ByteArray, wanted: Set<String>?): Pair<XmlTvStats, List<XmlTvProgramme>> {
        val out = mutableListOf<XmlTvProgramme>()
        val stats = XmlTvStreams.reader(ByteArrayInputStream(input)).use { reader ->
            parser.parse(reader, wanted, windowStart = t0, windowEnd = t0 + 24 * hour) { out += it }
        }
        return stats to out
    }

    @Test
    fun filtersByKeyAndWindow() {
        val (stats, items) = run(xml.toByteArray(), setOf(EpgKey.normalize("3CatInfo.es")!!))
        assertEquals(2, stats.channels)
        assertEquals(7, stats.programmesSeen)
        assertEquals(listOf("Noticias & más", "No stop"), items.map { it.title })
        assertEquals(items.size, stats.programmesKept)

        val first = items[0]
        assertEquals("3catinfoes", first.channelKey)
        assertEquals(t0, first.start)
        assertEquals(t0 + hour, first.stop)
        assertEquals("Informativo", first.desc)

        val noStop = items[1]
        assertEquals(t0 + hour, noStop.start)
        assertEquals(t0 + 2 * hour, noStop.stop)
        assertNull(noStop.desc)
    }

    @Test
    fun nullKeysKeepsAllChannels() {
        val (_, items) = run(xml.toByteArray(), null)
        assertTrue(items.any { it.title == "Not wanted" })
    }

    @Test
    fun gzipIsDetectedByMagicBytes() {
        val gz = ByteArrayOutputStream().also { bos ->
            GZIPOutputStream(bos).use { it.write(xml.toByteArray()) }
        }.toByteArray()
        val (_, items) = run(gz, setOf("3catinfoes"))
        assertEquals(2, items.size)
    }

    @Test
    fun leadingBomIsSkipped() {
        val gz = ByteArrayOutputStream().also { bos ->
            GZIPOutputStream(bos).use { it.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())); it.write(xml.toByteArray()) }
        }.toByteArray()
        val (_, items) = run(gz, setOf("3catinfoes"))
        assertEquals(2, items.size)
    }

    @Test(expected = Exception::class)
    fun malformedXmlThrows() {
        run("<tv><programme channel=\"a\" start=\"20260928100000\"><title>x</tv>".toByteArray(), null)
    }
}
