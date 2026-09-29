package tv.hdonlinetv.compose.core.epg

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EpgKeyTest {
    @Test
    fun normalize() {
        assertEquals("3catinfoes", EpgKey.normalize("3CatInfo.es"))
        assertEquals("3catinfoes", EpgKey.normalize(" 3catinfo.es@HD "))
        assertEquals("canal24horases", EpgKey.normalize("Canal.24.horas.es"))
        assertEquals("amcconnectes", EpgKey.normalize("AMC+.Connect.es"))
        assertNull(EpgKey.normalize(null))
        assertNull(EpgKey.normalize(""))
        assertNull(EpgKey.normalize("  "))
        assertNull(EpgKey.normalize("@HD"))
    }

    /** Same vectors as `ContractVectors` in epg-index `tests/test_api.py`. */
    @Test
    fun serverContract() {
        assertEquals("bbcnewsuk", EpgKey.normalize("BBCNews.uk@HD"))
        assertEquals("canalsurandalucia", EpgKey.nameKey("Canal Sur Andalucía (1080p) [Geo-blocked]"))
        assertEquals("cnn", EpgKey.nameKey("CNN HD"))
        assertEquals("daserste", EpgKey.nameKey("Das Erste 720p"))
        assertEquals("apunt", EpgKey.nameKey("À Punt (720p)"))
        assertEquals("hdtv", EpgKey.nameKey("HDTV"))
        assertNull(EpgKey.nameKey("(720p)"))
        assertNull(EpgKey.nameKey("Первый канал"))
        assertEquals(38, EpgKey.shardOf("123456789", 256))
    }
}
