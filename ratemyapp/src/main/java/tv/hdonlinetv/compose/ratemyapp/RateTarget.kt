package tv.hdonlinetv.compose.ratemyapp

/**
 * One way to open the store page.
 * @property uri ACTION_VIEW data.
 * @property targetPackage package to pin the Intent to, null = no setPackage (any handler).
 */
data class RateTarget(val uri: String, val targetPackage: String?)

/** Web fallback store for a build flavor (BuildConfig.STORE): "rustore" -> RUSTORE, else GPLAY. */
fun webFallbackFor(storeFlavor: String): Store =
    if (storeFlavor.trim().equals("rustore", ignoreCase = true)) Store.RUSTORE else Store.GPLAY

/**
 * Ordered candidates; the app takes the first one it can handle:
 * 1. the installer store app (only for GPLAY / RUSTORE), pinned to its package;
 * 2. market:// without a package (any store app, user chooses);
 * 3. https page: RuStore when [webFallback] == RUSTORE (a rustore build never links to Google
 *    Play), otherwise [storeWebUri] of the installer store (ANY -> GPLAY).
 * Pure: checking installed apps / canHandle and starting the Intent is the app's job.
 *
 * @param webFallback [webFallbackFor] of the build flavor; GPLAY or RUSTORE.
 */
fun rateTargetCandidates(store: Store, packageName: String, webFallback: Store): List<RateTarget> =
    buildList {
        when (store) {
            Store.GPLAY -> add(RateTarget(storeUri(Store.GPLAY, packageName), INSTALLER_GPLAY))
            Store.RUSTORE -> add(RateTarget(storeUri(Store.RUSTORE, packageName), INSTALLER_RUSTORE))
            Store.ANY -> Unit
        }
        add(RateTarget(storeUri(Store.ANY, packageName), null))
        val web = when {
            webFallback == Store.RUSTORE -> storeWebUri(Store.RUSTORE, packageName)
            store == Store.ANY -> storeWebUri(Store.GPLAY, packageName)
            else -> storeWebUri(store, packageName)
        }
        add(RateTarget(web, null))
    }