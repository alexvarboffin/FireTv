package tv.hdonlinetv.compose.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier
import tv.hdonlinetv.compose.ui.mobile.theme.PhoneTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsRepository = LocalSettingsRepository(applicationContext)
        NightModeApplier.apply(settingsRepository.getSettings().nightMode)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhoneTheme {
                PhoneNavHost()
            }
        }
    }
}
