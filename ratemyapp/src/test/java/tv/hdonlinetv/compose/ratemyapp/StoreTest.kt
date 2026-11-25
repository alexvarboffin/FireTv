package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
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

    @Test fun candidatesGplay() = assertEquals(
        listOf(
            RateTarget("market://details?id=a.b.c", "com.android.vending"),
            RateTarget("market://details?id=a.b.c", null),
            RateTarget("https://play.google.com/store/apps/details?id=a.b.c", null),
        ),
        rateTargetCandidates(Store.GPLAY, p),
    )

    @Test fun candidatesRustore() = assertEquals(
        listOf(
            RateTarget("rustore://apps.rustore.ru/app/a.b.c", "ru.vk.store"),
            RateTarget("market://details?id=a.b.c", null),
            RateTarget("https://www.rustore.ru/catalog/app/a.b.c", null),
        ),
        rateTargetCandidates(Store.RUSTORE, p),
    )

    @Test fun candidatesAny() = assertEquals(
        listOf(
            RateTarget("market://details?id=a.b.c", null),
            RateTarget("https://play.google.com/store/apps/details?id=a.b.c", null),
        ),
        rateTargetCandidates(Store.ANY, p),
    )
}