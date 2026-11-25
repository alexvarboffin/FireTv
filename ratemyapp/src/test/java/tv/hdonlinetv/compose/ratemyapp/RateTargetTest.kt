package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Test

class RateTargetTest {
    private val pkg = "tv.hdonlinetv.besttvchannels.movies.watchfree"
    private val market = "market://details?id=$pkg"
    private val rustore = "rustore://apps.rustore.ru/app/$pkg"

    @Test fun installerPresentAndHandles_returnsInstallerStoreUri() {
        assertEquals(rustore, resolveRateTarget(pkg, INSTALLER_RUSTORE) { true })
        assertEquals(market, resolveRateTarget(pkg, INSTALLER_GPLAY) { true })
    }

    @Test fun installerUnknown_marketResolves_returnsPlainMarket() {
        assertEquals(market, resolveRateTarget(pkg, null) { it == market })
        assertEquals(market, resolveRateTarget(pkg, "com.amazon.venezia") { it == market })
    }

    @Test fun nothingResolves_returnsHttps() {
        assertEquals(
            "https://play.google.com/store/apps/details?id=$pkg",
            resolveRateTarget(pkg, null) { false },
        )
        assertEquals(
            "https://www.rustore.ru/catalog/app/$pkg",
            resolveRateTarget(pkg, INSTALLER_RUSTORE) { false },
        )
    }

    @Test fun installerKnownButCannotHandle_fallsBackToMarket() {
        val asked = mutableListOf<String>()
        val uri = resolveRateTarget(pkg, INSTALLER_RUSTORE) { asked += it; it == market }
        assertEquals(market, uri)
        assertEquals(listOf(rustore, market), asked)
    }
}