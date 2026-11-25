package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreTest {
    private val p = "a.b.c"

    @Test fun selectByInstaller() {
        assertEquals(Store.GPLAY, selectStore("com.android.vending"))
        assertEquals(Store.RUSTORE, selectStore("ru.vk.store"))
        assertEquals(Store.ANY, selectStore("com.amazon.venezia"))
        assertEquals(Store.ANY, selectStore(null))
    }

    @Test fun uris() {
        assertEquals("market://details?id=a.b.c", storeUri(Store.GPLAY, p))
        assertEquals("market://details?id=a.b.c", storeUri(Store.ANY, p))
        assertEquals("rustore://apps.rustore.ru/app/a.b.c", storeUri(Store.RUSTORE, p))
        assertEquals("https://play.google.com/store/apps/details?id=a.b.c", storeWebUri(Store.GPLAY, p))
        assertEquals("https://play.google.com/store/apps/details?id=a.b.c", storeWebUri(Store.ANY, p))
        assertEquals("https://www.rustore.ru/catalog/app/a.b.c", storeWebUri(Store.RUSTORE, p))
    }

    private val play = "https://play.google.com/store/apps/details?id=a.b.c"
    private val ru = "https://www.rustore.ru/catalog/app/a.b.c"
    private val market = "market://details?id=a.b.c"

    @Test fun webFallbackForFlavor() {
        assertEquals(Store.RUSTORE, webFallbackFor("rustore"))
        assertEquals(Store.RUSTORE, webFallbackFor(" RuStore "))
        assertEquals(Store.GPLAY, webFallbackFor("gplay"))
        assertEquals(Store.GPLAY, webFallbackFor(""))
    }

    @Test fun candidatesGplay() = assertEquals(
        listOf(RateTarget(market, "com.android.vending"), RateTarget(market, null), RateTarget(play, null)),
        rateTargetCandidates(Store.GPLAY, p, webFallback = Store.GPLAY),
    )

    @Test fun candidatesAnyGplayFlavor() = assertEquals(
        listOf(RateTarget(market, null), RateTarget(play, null)),
        rateTargetCandidates(Store.ANY, p, webFallback = Store.GPLAY),
    )

    @Test fun rustoreInstallerGetsRustoreHttpsInBothFlavors() {
        for (flavor in listOf(Store.GPLAY, Store.RUSTORE)) assertEquals(
            listOf(
                RateTarget("rustore://apps.rustore.ru/app/a.b.c", "ru.vk.store"),
                RateTarget(market, null),
                RateTarget(ru, null),
            ),
            rateTargetCandidates(Store.RUSTORE, p, webFallback = flavor),
        )
    }

    @Test fun rustoreFlavorNeverLinksToPlay() {
        for (store in Store.values()) {
            val c = rateTargetCandidates(store, p, webFallback = webFallbackFor("rustore"))
            assertTrue(c.none { "play.google.com" in it.uri })
            assertEquals(RateTarget(ru, null), c.last())
        }
    }
}