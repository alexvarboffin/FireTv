package tv.hdonlinetv.compose.rate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toUri
import tv.hdonlinetv.compose.ratemyapp.resolveRateTarget
import tv.hdonlinetv.compose.util.startActivitySafe

/** Package that installed this app (RuStore, Play, Amazon, ...), or null if unknown / sideloaded. */
fun Context.installerPackageOrNull(): String? = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        packageManager.getInstallSourceInfo(packageName).installingPackageName
    } else {
        @Suppress("DEPRECATION")
        packageManager.getInstallerPackageName(packageName)
    }
} catch (_: PackageManager.NameNotFoundException) {
    null
} catch (_: IllegalArgumentException) {
    null
}

private fun viewIntent(uri: String): Intent = Intent(Intent.ACTION_VIEW, uri.toUri())

private fun Context.resolves(intent: Intent): Boolean =
    packageManager.resolveActivity(intent, 0) != null

/**
 * UTV-R5 "Rate" intent: URI from [resolveRateTarget]; if the installer itself can open it,
 * the intent is pinned to the installer (no chooser, no foreign store).
 */
fun Context.buildRateIntent(): Intent {
    val installer = installerPackageOrNull()
    val uri = resolveRateTarget(packageName, installer) { resolves(viewIntent(it)) }
    val intent = viewIntent(uri)
    if (installer != null) {
        val pinned = Intent(intent).setPackage(installer)
        if (resolves(pinned)) return pinned
    }
    return intent
}

/** Click handler for menu_rate (mobile + TV drawers). */
fun Context.openRateStore() {
    startActivitySafe(buildRateIntent())
}