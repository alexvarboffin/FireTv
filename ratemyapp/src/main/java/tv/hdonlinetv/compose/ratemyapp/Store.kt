package tv.hdonlinetv.compose.ratemyapp

/** Store the rating request targets. [ANY] = unknown installer: let the user choose. */
enum class Store { GPLAY, RUSTORE, ANY }

/** Installer package of Google Play. */
const val INSTALLER_GPLAY: String = "com.android.vending"

/** Installer package of RuStore. */
const val INSTALLER_RUSTORE: String = "ru.vk.store"

/** Store by installer package: Play -> GPLAY, RuStore -> RUSTORE, anything else / null -> ANY. */
fun selectStore(installerPackage: String?): Store = when (installerPackage) {
    INSTALLER_GPLAY -> Store.GPLAY
    INSTALLER_RUSTORE -> Store.RUSTORE
    else -> Store.ANY
}

/**
 * Store deep link.
 * GPLAY: market:// (caller pins package [INSTALLER_GPLAY]).
 * ANY: the same market:// URI, meant for an Intent without a package (system chooser).
 * RUSTORE: rustore:// app card.
 */
fun storeUri(store: Store, packageName: String): String = when (store) {
    Store.GPLAY, Store.ANY -> "market://details?id=$packageName"
    Store.RUSTORE -> "rustore://apps.rustore.ru/app/$packageName"
}

/** https fallback when no app can handle [storeUri]. ANY falls back to Google Play web. */
fun storeWebUri(store: Store, packageName: String): String = when (store) {
    Store.GPLAY, Store.ANY -> "https://play.google.com/store/apps/details?id=$packageName"
    Store.RUSTORE -> "https://www.rustore.ru/catalog/app/$packageName"
}