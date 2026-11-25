package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class RateTargetTest {
    private val pkg = "tv.hdonlinetv.besttvchannels.movies.watchfree"
    private val market = "market://details?id=$pkg"
    private val rustore = "rustore://apps.rustore.ru/app/$pkg"
    private val playWeb = "https://play.google.com/store/apps/details?id=$pkg"
    private val rustoreWeb = "https://www.rustore.ru/catalog/app/$pkg"

    @Test fun gplayInstallerHandles_returnsMarketPinnedToPlay() {
        assertEquals(
            RateTarget(market, INSTALLER_GPLAY),
            resolveRateTarget(pkg, INSTALLER_GPLAY, Store.GPLAY) { true },
        )
    }

    @Test fun rustoreInstallerNotInstalled_fallsBackToMarketWithoutPackage() {
        val asked = mutableListOf<RateTarget>()
        val target = resolveRateTarget(pkg, INSTALLER_RUSTORE, Store.GPLAY) {
            asked += it
            it.targetPackage == null && it.uri.startsWith("market://")
        }
        assertEquals(RateTarget(market, null), target)
        assertEquals(listOf(RateTarget(rustore, INSTALLER_RUSTORE), RateTarget(market, null)), asked)
    }

    @Test fun unknownInstaller_returnsMarketWithFullIdAndNoPackage() {
        assertEquals(RateTarget(market, null), resolveRateTarget(pkg, "com.amazon.venezia", Store.GPLAY) { true })
        assertEquals(RateTarget(market, null), resolveRateTarget(pkg, null, Store.GPLAY) { true })
    }

    @Test fun nothingHandlesMarket_returnsHttps() {
        assertEquals(
            RateTarget(playWeb, null),
            resolveRateTarget(pkg, null, Store.GPLAY) { !it.uri.startsWith("market://") },
        )
        assertEquals(
            RateTarget(rustoreWeb, null),
            resolveRateTarget(pkg, INSTALLER_RUSTORE, Store.GPLAY) { false },
        )
    }

    @Test fun rustoreWebFallback_nothingOpenable_returnsRustoreHttpsAndNeverPlay() {
        for (installer in listOf(null, INSTALLER_GPLAY, INSTALLER_RUSTORE, "com.amazon.venezia")) {
            val seen = mutableListOf<RateTarget>()
            val target = resolveRateTarget(pkg, installer, Store.RUSTORE) { seen += it; false }
            assertEquals("installer=$installer", RateTarget(rustoreWeb, null), target)
            assertFalse("installer=$installer", (seen + target).any { "play.google.com" in it.uri })
        }
    }
}