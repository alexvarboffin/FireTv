package tv.hdonlinetv.compose.rate

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.net.toUri
import tv.hdonlinetv.compose.ratemyapp.RateTarget
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

/** ACTION_VIEW intent for [target], pinned to [RateTarget.targetPackage] when set. */
fun RateTarget.toIntent(): Intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
    targetPackage?.let { setPackage(it) }
}

private fun Context.canHandle(target: RateTarget): Boolean =
    packageManager.resolveActivity(target.toIntent(), 0) != null

/** UTV-R5 "Rate" intent: first store candidate this device can open (https as last resort). */
fun Context.buildRateIntent(): Intent =
    resolveRateTarget(packageName, installerPackageOrNull()) { canHandle(it) }.toIntent()

/** Click handler for menu_rate (mobile + TV drawers). */
fun Context.openRateStore() {
    startActivitySafe(buildRateIntent())
}