package tv.hdonlinetv.compose.ratemyapp

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Koin module of :ratemyapp. Needs androidContext(). To change thresholds, load a module
 * after this one with `single { RateConfig(...) }` (override) or replace the binding.
 */
val ratemyappModule = module {
    single { RateConfig() }
    single<RatePrefs> { SharedPreferencesRatePrefs(androidContext()) }
    single<RateMyAppController> { DefaultRateMyAppController(get(), get()) }
}