package tv.hdonlinetv.compose.rate

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLaunchTest {
    @Test fun freshStartCounts() = assertTrue(shouldCountLaunch(isRestored = false))

    @Test fun restoredStartDoesNotCount() = assertFalse(shouldCountLaunch(isRestored = true))
}