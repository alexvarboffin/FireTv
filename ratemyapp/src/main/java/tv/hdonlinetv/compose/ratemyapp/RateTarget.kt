package tv.hdonlinetv.compose.ratemyapp

/**
 * One way to open the store page.
 * @property uri ACTION_VIEW data.
 * @property targetPackage package to pin the Intent to, null = no setPackage (any handler).
 */
data class RateTarget(val uri: String, val targetPackage: String?)

/**
 * Ordered candidates; the app takes the first one it can handle:
 * 1. the installer store app (only for GPLAY / RUSTORE), pinned to its package;
 * 2. market:// without a package (any store app, user chooses);
 * 3. https web page of the store ([storeWebUri]).
 * Pure: checking installed apps / canHandle and starting the Intent is the app's job.
 */
fun rateTargetCandidates(store: Store, packageName: String): List<RateTarget> = buildList {
    when (store) {
        Store.GPLAY -> add(RateTarget(storeUri(Store.GPLAY, packageName), INSTALLER_GPLAY))
        Store.RUSTORE -> add(RateTarget(storeUri(Store.RUSTORE, packageName), INSTALLER_RUSTORE))
        Store.ANY -> Unit
    }
    add(RateTarget(storeUri(Store.ANY, packageName), null))
    add(RateTarget(storeWebUri(store, packageName), null))
}

/**
 * UTV-R5: first candidate of [rateTargetCandidates] the device can handle;
 * the last candidate (https) is the final fallback even if [canHandle] rejects it.
 * Pure: [canHandle] is supplied by the app (Intent + resolveActivity).
 */
fun resolveRateTarget(
    packageName: String,
    installerPackage: String?,
    canHandle: (RateTarget) -> Boolean,
): RateTarget {
    val candidates = rateTargetCandidates(selectStore(installerPackage), packageName)
    return candidates.firstOrNull(canHandle) ?: candidates.last()
}