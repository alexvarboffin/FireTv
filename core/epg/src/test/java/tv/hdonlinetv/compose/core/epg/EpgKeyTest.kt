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
}
