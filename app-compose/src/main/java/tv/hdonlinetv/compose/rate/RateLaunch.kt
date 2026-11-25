package tv.hdonlinetv.compose.rate

private const val ACTION_MAIN = "android.intent.action.MAIN"
private const val CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER"
private const val CATEGORY_LEANBACK_LAUNCHER = "android.intent.category.LEANBACK_LAUNCHER"

/**
 * UTV-R6c: count a rate-me launch only for a fresh user start of the launcher Activity:
 * not a recreated one (rotation / process-death restore: savedInstanceState != null)
 * and not one stacked on a live task (not task root).
 */
fun shouldCountLaunch(isRestored: Boolean, isTaskRoot: Boolean): Boolean = !isRestored && isTaskRoot

/**
 * Standard launcher re-entry guard: a MAIN/LAUNCHER (or LEANBACK_LAUNCHER) intent that landed on
 * top of an existing task (e.g. OneSignal's getLaunchIntentForPackage with NEW_TASK |
 * RESET_TASK_IF_NEEDED, which differs from the home-screen intent by setPackage).
 */
fun isLauncherReentry(isTaskRoot: Boolean, action: String?, categories: Set<String>?): Boolean =
    !isTaskRoot && action == ACTION_MAIN && categories != null &&
        (CATEGORY_LAUNCHER in categories || CATEGORY_LEANBACK_LAUNCHER in categories)