package tv.hdonlinetv.compose.rate

/**
 * UTV-R6c: count a rate-me launch only for a fresh user start of the launcher Activity,
 * not for a recreated one (rotation / process-death restore: savedInstanceState != null).
 */
fun shouldCountLaunch(isRestored: Boolean): Boolean = !isRestored