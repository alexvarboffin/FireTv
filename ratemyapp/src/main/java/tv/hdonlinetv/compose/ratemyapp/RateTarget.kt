package tv.hdonlinetv.compose.ratemyapp

/**
 * UTV-R5: which URI the "Rate" menu item opens. Pure JVM, no Context.
 *
 * 1. Installer is a known store and its own deep link resolves -> that store (rustore:// / market://).
 * 2. Otherwise a plain market://details?id=<pkg> if anything resolves it (any store, no package pinned).
 * 3. Otherwise https: RuStore web page if RuStore installed the app, else Google Play web page.
 *
 * @param canHandle true if some activity can open the URI with ACTION_VIEW.
 */
fun resolveRateTarget(
    packageName: String,
    installerPackage: String?,
    canHandle: (uri: String) -> Boolean,
): String {
    val installerStore = knownInstallerStore(installerPackage)
    if (installerStore != null) {
        val installerUri = storeUri(installerStore, packageName)
        if (canHandle(installerUri)) return installerUri
    }
    val marketUri = anyMarketUri(packageName)
    if (canHandle(marketUri)) return marketUri
    return storeWebUri(installerStore ?: Store.GPLAY, packageName)
}

/** Store of a known installer; null for sideload / unknown installers (no flavor fallback here). */
private fun knownInstallerStore(installerPackage: String?): Store? = when (installerPackage) {
    INSTALLER_GPLAY, INSTALLER_RUSTORE -> selectStore(FLAVOR_GPLAY, installerPackage)
    else -> null
}

/** Plain market:// link any store can take. Switch to storeUri(Store.ANY, ...) once it exists. */
private fun anyMarketUri(packageName: String): String = storeUri(Store.GPLAY, packageName)