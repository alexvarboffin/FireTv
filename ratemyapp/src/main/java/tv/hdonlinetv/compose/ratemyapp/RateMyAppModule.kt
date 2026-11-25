package tv.hdonlinetv.compose.ratemyapp

import android.app.UiModeManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** True on Android TV / leanback devices. */
fun isTvDevice(context: Context): Boolean {
    val uiMode = context.getSystemService(Context.UI_MODE_SERVICE) as? UiModeManager
    if (uiMode?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION) return true
    return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
}

/**
 * Koin module of :ratemyapp. Needs androidContext(). To change thresholds, load a module
 * after this one with `single { RateConfig(...) }` (override) or replace the binding.
 */
val ratemyappModule = module {
    single { RateConfig() }
    single<RatePrefs> { SharedPreferencesRatePrefs(androidContext()) }
    single<RateMyAppController> {
        val context = androidContext()
        val tv by lazy { isTvDevice(context) }
        DefaultRateMyAppController(get(), get(), isTv = { tv })
    }
}