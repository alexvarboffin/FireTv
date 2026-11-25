package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateMyAppControllerTest {
    private class FakePrefs : RatePrefs {
        override var launchCount = 0
        override var firstLaunchMillis = 0L
        override var anchorMillis = 0L
        override var laterCount = 0
        override var completed = false
    }

    private val min = 60L * 1000
    private val day = 24L * 60 * min
    private var clock = 1_000_000L
    private var tv = false
    private val prefs = FakePrefs()

    private fun controller(config: RateConfig = RateConfig()) =
        DefaultRateMyAppController(config, prefs, isTv = { tv }) { clock }

    private val c = controller()
    private fun launch(n: Int) = repeat(n) { c.onAppLaunched() }

    /** 5 launches, then 60 min, then one more launch -> AskRating. */
    private fun ready() { launch(5); clock += 60 * min; c.onAppLaunched() }

    @Test fun legacyDefaults() {
        val d = RateConfig()
        assertEquals(5, d.minLaunches)
        assertEquals(listOf(60, 120, 180), d.pauseStepsMinutes)
        assertEquals(0, d.minDaysSinceInstall)
        assertEquals(5, d.positiveThreshold)
        assertTrue(d.starFilterEnabled)
        assertFalse(d.showOnTv)
        assertEquals(null, d.feedbackEmail)
    }

    @Test fun needsMinLaunches() {
        launch(4)
        clock += 10 * day
        c.onAppLaunched() // 5th launch arms the pause now
        assertEquals(RateUiState.Hidden, c.state.value)
        assertFalse(c.shouldShow())
    }

    @Test fun firstAskSixtyMinutesAfterArming() {
        launch(5)
        assertEquals(clock, prefs.anchorMillis)
        clock += 59 * min
        assertFalse(c.shouldShow())
        clock += min
        assertTrue(c.shouldShow())
        c.onAppLaunched()
        assertEquals(RateUiState.AskRating, c.state.value)
    }

    @Test fun laterLadder120Then180ThenStops() {
        ready()
        c.onLater()
        assertEquals(RateUiState.Hidden, c.state.value)
        clock += 119 * min; assertFalse(c.shouldShow())
        clock += min; assertTrue(c.shouldShow())
        c.onLater()
        clock += 179 * min; assertFalse(c.shouldShow())
        clock += min; assertTrue(c.shouldShow())
        c.onLater()
        assertTrue(prefs.completed)
        clock += 365 * day
        assertFalse(c.shouldShow())
    }

    @Test fun fiveStarsOpensStore() {
        ready()
        c.onRated(5)
        assertEquals(RateUiState.OpenStore, c.state.value)
        c.onStoreOpened()
        assertEquals(RateUiState.Hidden, c.state.value)
        assertTrue(prefs.completed)
    }

    @Test fun belowThresholdThankYouWithEmail() {
        val e = controller(RateConfig(feedbackEmail = "dev@example.org"))
        e.onRated(4)
        assertEquals(RateUiState.ThankYou(4, "dev@example.org"), e.state.value)
        e.onThankYouDone()
        assertEquals(RateUiState.Hidden, e.state.value)
    }

    @Test fun belowThresholdThankYouDefaultNoEmail() {
        ready()
        c.onRated(4)
        assertEquals(RateUiState.ThankYou(4, null), c.state.value)
        c.onThankYouDone()
        assertEquals(RateUiState.Hidden, c.state.value)
        assertTrue(prefs.completed)
        c.onAppLaunched()
        assertEquals(RateUiState.Hidden, c.state.value)
    }

    @Test fun starFilterOffAlwaysStore() {
        val nf = controller(RateConfig(starFilterEnabled = false))
        nf.onRated(1)
        assertEquals(RateUiState.OpenStore, nf.state.value)
    }

    @Test fun nullEmailPassedThrough() {
        val ne = controller(RateConfig(feedbackEmail = null))
        ne.onRated(2)
        assertEquals(RateUiState.ThankYou(2, null), ne.state.value)
    }

    @Test fun tvHiddenByDefault() {
        tv = true
        ready()
        assertFalse(c.shouldShow())
        assertEquals(RateUiState.Hidden, c.state.value)
    }

    @Test fun tvAllowedWhenShowOnTv() {
        tv = true
        val t = controller(RateConfig(showOnTv = true))
        repeat(5) { t.onAppLaunched() }
        clock += 60 * min
        t.onAppLaunched()
        assertEquals(RateUiState.AskRating, t.state.value)
    }

    @Test fun minDaysGate() {
        val d = controller(RateConfig(minDaysSinceInstall = 1))
        repeat(5) { d.onAppLaunched() }
        clock += 60 * min
        assertFalse(d.shouldShow())
        clock += day
        assertTrue(d.shouldShow())
    }

    @Test fun neverStopsForever() {
        ready()
        c.onNever()
        clock += 365 * day
        launch(10)
        assertFalse(c.shouldShow())
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStarsRejected() = c.onRated(0)
}