package com.walhalla.data.repository

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.regex.Pattern

class M3UParserHeaderTest {

    private val body = """
        #EXTINF:-1 tvg-id="24Horas.es" tvg-logo="http://logo/24h.png" group-title="News",24 Horas (Spain) (720p)
        http://stream.example/24h.m3u8
        #EXTINF:-1 tvg-id="" group-title="Movies",No Id Channel
        #EXTVLCOPT:http-user-agent=Mozilla/5.0
        http://stream.example/noid.m3u8
        #EXTINF:-1 tvg-id="5Cops.uk" group-title="Series",5 Cops
        http://stream.example/5cops.m3u8
    """.trimIndent()

    private val playlists = mapOf(
        "no header" to body,
        "bare header" to "#EXTM3U\n$body",
        "url-tvg" to "#EXTM3U url-tvg=\"https://epg.example/guide.xml.gz\"\n$body",
        "x-tvg-url multi" to
            "#EXTM3U x-tvg-url=\"https://a.example/1.xml, https://b.example/2.xml.gz\"\n$body",
        "bom crlf" to
            ("\uFEFF#EXTM3U url-tvg=\"https://epg.example/guide.xml\"\n$body").replace("\n", "\r\n"),
    )

    @Test
    fun headerUrls_absentOrPresent() {
        assertEquals(emptyList<String>(), M3UParser.parseHeaderEpgUrls(playlists.getValue("no header")))
        assertEquals(emptyList<String>(), M3UParser.parseHeaderEpgUrls(playlists.getValue("bare header")))
        assertEquals(
            listOf("https://epg.example/guide.xml.gz"),
            M3UParser.parseHeaderEpgUrls(playlists.getValue("url-tvg")),
        )
        assertEquals(
            listOf("https://a.example/1.xml", "https://b.example/2.xml.gz"),
            M3UParser.parseHeaderEpgUrls(playlists.getValue("x-tvg-url multi")),
        )
        assertEquals(
            listOf("https://epg.example/guide.xml"),
            M3UParser.parseHeaderEpgUrls(playlists.getValue("bom crlf")),
        )
    }

    @Test
    fun headerUrls_edgeCases() {
        assertEquals(emptyList<String>(), M3UParser.parseHeaderEpgUrls(""))
        assertEquals(emptyList<String>(), M3UParser.parseHeaderEpgUrls("#EXTM3U url-tvg=\"\""))
        assertEquals(emptyList<String>(), M3UParser.parseHeaderEpgUrls("#EXTM3U url-tvg=\"not a url\""))
        assertEquals(
            listOf("http://x.example/g.xml"),
            M3UParser.parseHeaderEpgUrls("\n\n  #extm3u URL-TVG=\"http://x.example/g.xml\" x-tvg-url=\"http://x.example/g.xml\""),
        )
        // tvg-url inside #EXTINF is per-channel, not a playlist header.
        assertEquals(
            emptyList<String>(),
            M3UParser.parseHeaderEpgUrls("#EXTINF:-1 x-tvg-url=\"http://x.example/g.xml\",A\nhttp://a"),
        )
    }

    /** Channel-block regex of [M3UParser.parseM3U] yields identical channels with any header. */
    @Test
    fun channelBlocks_unchangedByHeader() {
        val expected = listOf(
            "24Horas.es" to "http://stream.example/24h.m3u8",
            "" to "http://stream.example/noid.m3u8",
            "5Cops.uk" to "http://stream.example/5cops.m3u8",
        )
        playlists.forEach { (label, text) ->
            assertEquals(label, expected, channelBlocks(text))
        }
        assertTrue(M3UParser.parseHeaderEpgUrls(body).isEmpty())
    }

    private fun channelBlocks(text: String): List<Pair<String, String>> {
        val tvgId = Pattern.compile("tvg-id=\"([^\"]*)\"")
        val matcher = Pattern.compile(M3UParser.regex, Pattern.MULTILINE).matcher(text)
        val out = mutableListOf<Pair<String, String>>()
        while (matcher.find()) {
            val attrs = matcher.group(1).orEmpty()
            val id = tvgId.matcher(attrs).let { if (it.find()) it.group(1) else null }.orEmpty()
            out += id to matcher.group(4).orEmpty().trim()
        }
        return out
    }
}
