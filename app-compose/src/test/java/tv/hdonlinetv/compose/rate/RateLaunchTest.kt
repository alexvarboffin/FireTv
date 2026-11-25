package tv.hdonlinetv.compose.rate

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RateLaunchTest {
    private val main = "android.intent.action.MAIN"
    private val launcher = setOf("android.intent.category.LAUNCHER")
    private val leanback = setOf("android.intent.category.LEANBACK_LAUNCHER")

    @Test fun freshRootCounts() = assertTrue(shouldCountLaunch(isRestored = false, isTaskRoot = true))

    @Test fun restoredDoesNotCount() = assertFalse(shouldCountLaunch(isRestored = true, isTaskRoot = true))

    @Test fun notTaskRootDoesNotCount() = assertFalse(shouldCountLaunch(isRestored = false, isTaskRoot = false))

    @Test fun launcherIntentOnTopOfLiveTask_isReentry() {
        assertTrue(isLauncherReentry(isTaskRoot = false, action = main, categories = launcher))
        assertTrue(isLauncherReentry(isTaskRoot = false, action = main, categories = leanback))
    }

    @Test fun taskRootOrNonLauncherIntent_isNotReentry() {
        assertFalse(isLauncherReentry(isTaskRoot = true, action = main, categories = launcher))
        assertFalse(isLauncherReentry(isTaskRoot = false, action = "android.intent.action.VIEW", categories = launcher))
        assertFalse(isLauncherReentry(isTaskRoot = false, action = main, categories = null))
        assertFalse(isLauncherReentry(isTaskRoot = false, action = null, categories = launcher))
    }
}