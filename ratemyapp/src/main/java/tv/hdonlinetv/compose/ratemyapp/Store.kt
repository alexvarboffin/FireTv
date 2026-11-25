package tv.hdonlinetv.compose.ratemyapp

/** App store the rating request targets. */
enum class Store { GPLAY, RUSTORE }

/** Installer package of Google Play. */
const val INSTALLER_GPLAY: String = "com.android.vending"

/** Installer package of RuStore. */
const val INSTALLER_RUSTORE: String = "ru.vk.store"

/** BuildConfig.STORE values of :app-compose. */
const val FLAVOR_GPLAY: String = "gplay"
const val FLAVOR_RUSTORE: String = "rustore"

/**
 * Picks the store: the actual installer wins, otherwise the build flavor.
 * Unknown flavor falls back to [Store.GPLAY].
 *
 * @param storeFlavor BuildConfig.STORE ("gplay" / "rustore").
 * @param installerPackage installer package name, or null if unknown / sideloaded.
 */
fun selectStore(storeFlavor: String, installerPackage: String?): Store = when (installerPackage) {
    INSTALLER_GPLAY -> Store.GPLAY
    INSTALLER_RUSTORE -> Store.RUSTORE
    else -> when (storeFlavor.trim().lowercase()) {
        FLAVOR_RUSTORE -> Store.RUSTORE
        else -> Store.GPLAY
    }
}

/** Store app deep link (market:// for Google Play, rustore:// for RuStore). */
fun storeUri(store: Store, packageName: String): String = when (store) {
    Store.GPLAY -> "market://details?id=$packageName"
    Store.RUSTORE -> "rustore://apps.rustore.ru/app/$packageName"
}

/** https fallback for when the store app is not installed. */
fun storeWebUri(store: Store, packageName: String): String = when (store) {
    Store.GPLAY -> "https://play.google.com/store/apps/details?id=$packageName"
    Store.RUSTORE -> "https://www.rustore.ru/catalog/app/$packageName"
}