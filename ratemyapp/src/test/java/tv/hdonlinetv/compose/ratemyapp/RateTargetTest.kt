package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Test

class RateTargetTest {
    private val pkg = "tv.hdonlinetv.besttvchannels.movies.watchfree"
    private val market = "market://details?id=$pkg"
    private val rustore = "rustore://apps.rustore.ru/app/$pkg"
    private val playWeb = "https://play.google.com/store/apps/details?id=$pkg"

    @Test fun gplayInstallerHandles_returnsMarketPinnedToPlay() {
        assertEquals(
            RateTarget(market, INSTALLER_GPLAY),
            resolveRateTarget(pkg, INSTALLER_GPLAY) { true },
        )
    }

    @Test fun rustoreInstallerNotInstalled_fallsBackToMarketWithoutPackage() {
        val asked = mutableListOf<RateTarget>()
        val target = resolveRateTarget(pkg, INSTALLER_RUSTORE) {
            asked += it
            it.targetPackage == null && it.uri.startsWith("market://")
        }
        assertEquals(RateTarget(market, null), target)
        assertEquals(listOf(RateTarget(rustore, INSTALLER_RUSTORE), RateTarget(market, null)), asked)
    }

    @Test fun unknownInstaller_returnsMarketWithFullIdAndNoPackage() {
        assertEquals(RateTarget(market, null), resolveRateTarget(pkg, "com.amazon.venezia") { true })
        assertEquals(RateTarget(market, null), resolveRateTarget(pkg, null) { true })
    }

    @Test fun nothingHandlesMarket_returnsHttps() {
        assertEquals(
            RateTarget(playWeb, null),
            resolveRateTarget(pkg, null) { !it.uri.startsWith("market://") },
        )
        assertEquals(
            RateTarget("https://www.rustore.ru/catalog/app/$pkg", null),
            resolveRateTarget(pkg, INSTALLER_RUSTORE) { false },
        )
    }
}