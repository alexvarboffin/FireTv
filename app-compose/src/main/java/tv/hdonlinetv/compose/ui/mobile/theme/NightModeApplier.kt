package tv.hdonlinetv.compose.ui.mobile.theme

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
}
