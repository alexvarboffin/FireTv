package tv.hdonlinetv.compose.ratemyapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateMyAppControllerTest {
    private class FakePrefs : RatePrefs {
        override var launchCount = 0
        override var firstLaunchMillis = 0L
        override var remindAtMillis = 0L
        override var completed = false
    }

    private val day = 24L * 60 * 60 * 1000
    private var clock = 1_000_000L
    private val prefs = FakePrefs()
    private val config = RateConfig(minLaunches = 3, minDaysSinceInstall = 2, remindAfterDays = 5, positiveThreshold = 4)
    private val c = DefaultRateMyAppController(config, prefs) { clock }

    private fun launch(n: Int) = repeat(n) { c.onAppLaunched() }

    @Test fun hiddenUntilLaunchesAndDaysReached() {
        launch(3)
        assertEquals(RateUiState.Hidden, c.state.value)
        assertFalse(c.shouldShow())
        clock += 2 * day
        assertTrue(c.shouldShow())
        c.onAppLaunched()
        assertEquals(RateUiState.AskRating, c.state.value)
        assertEquals(4, prefs.launchCount)
        assertEquals(1_000_000L, prefs.firstLaunchMillis)
    }

    @Test fun daysWithoutEnoughLaunchesDoesNotShow() {
        launch(1)
        clock += 10 * day
        c.onAppLaunched()
        assertEquals(RateUiState.Hidden, c.state.value)
    }

    private fun ready() { launch(3); clock += 2 * day; c.onAppLaunched() }

    @Test fun positiveRatingOpensStoreThenCompletes() {
        ready()
        c.onRated(4)
        assertEquals(RateUiState.OpenStore, c.state.value)
        c.onStoreOpened()
        assertEquals(RateUiState.Hidden, c.state.value)
        assertTrue(prefs.completed)
        c.onAppLaunched()
        assertEquals(RateUiState.Hidden, c.state.value)
    }

    @Test fun lowRatingHidesAndCompletesEvenWithForkFlag() {
        val forked = DefaultRateMyAppController(config.copy(lowStarsFork = true), prefs) { clock }
        forked.onRated(3)
        assertEquals(RateUiState.Hidden, forked.state.value)
        assertTrue(prefs.completed)
    }

    @Test fun laterSnoozes() {
        ready()
        c.onLater()
        assertEquals(RateUiState.Hidden, c.state.value)
        clock += 4 * day
        assertFalse(c.shouldShow())
        clock += day
        assertTrue(c.shouldShow())
    }

    @Test fun neverStopsForever() {
        ready()
        c.onNever()
        clock += 365 * day
        launch(10)
        assertFalse(c.shouldShow())
        assertEquals(RateUiState.Hidden, c.state.value)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidStarsRejected() = c.onRated(0)

    @Test fun lowStarsForkDefaultsOff() = assertFalse(RateConfig().lowStarsFork)
}