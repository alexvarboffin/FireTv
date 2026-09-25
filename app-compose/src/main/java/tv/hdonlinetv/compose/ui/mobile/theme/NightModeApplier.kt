package tv.hdonlinetv.compose.ui.mobile.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

object NightModeApplier {
    fun apply(nightMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (nightMode) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            },
        )
    }

    /** So `colorResource` / `values-night` follow the setting on FragmentActivity. */
    fun wrap(base: Context, nightMode: Boolean): Context {
        val nightBit = if (nightMode) {
            Configuration.UI_MODE_NIGHT_YES
        } else {
            Configuration.UI_MODE_NIGHT_NO
        }
        val config = Configuration(base.resources.configuration)
        val current = config.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (current == nightBit) return base
        config.uiMode = (config.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or nightBit
        return base.createConfigurationContext(config)
    }
}
