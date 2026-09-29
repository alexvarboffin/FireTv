package tv.hdonlinetv.compose.core.epg.xmltv

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XmlTvTimeTest {
    // 2026-09-28T00:30:00Z
    private val base = 1_790_555_400_000L

    @Test
    fun offsets() {
        assertEquals(base, XmlTvTime.parse("20260928003000 +0000"))
        assertEquals(base, XmlTvTime.parse("20260928003000"))
        assertEquals(base, XmlTvTime.parse("20260928023000 +0200"))
        assertEquals(base, XmlTvTime.parse("20260927203000 -0400"))
        assertEquals(base, XmlTvTime.parse("20260928053000+0500"))
        assertEquals(base, XmlTvTime.parse("20260928060000 +05:30"))
        assertEquals(base, XmlTvTime.parse("202609280030 +0000"))
    }

    @Test
    fun invalid() {
        assertNull(XmlTvTime.parse(null))
        assertNull(XmlTvTime.parse(""))
        assertNull(XmlTvTime.parse("tomorrow"))
    }
}
