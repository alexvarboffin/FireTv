package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Test

class StoreTest {
    @Test fun installerPlayWinsOverRustoreFlavor() =
        assertEquals(Store.GPLAY, selectStore("rustore", "com.android.vending"))

    @Test fun installerRustoreWinsOverGplayFlavor() =
        assertEquals(Store.RUSTORE, selectStore("gplay", "ru.vk.store"))

    @Test fun nullInstallerUsesFlavor() {
        assertEquals(Store.RUSTORE, selectStore("rustore", null))
        assertEquals(Store.GPLAY, selectStore("gplay", null))
    }

    @Test fun unknownInstallerUsesFlavor() =
        assertEquals(Store.RUSTORE, selectStore(" RuStore ", "com.amazon.venezia"))

    @Test fun unknownFlavorFallsBackToGplay() =
        assertEquals(Store.GPLAY, selectStore("", null))

    @Test fun uris() {
        val p = "a.b.c"
        assertEquals("market://details?id=a.b.c", storeUri(Store.GPLAY, p))
        assertEquals("rustore://apps.rustore.ru/app/a.b.c", storeUri(Store.RUSTORE, p))
        assertEquals("https://play.google.com/store/apps/details?id=a.b.c", storeWebUri(Store.GPLAY, p))
        assertEquals("https://www.rustore.ru/catalog/app/a.b.c", storeWebUri(Store.RUSTORE, p))
    }
}