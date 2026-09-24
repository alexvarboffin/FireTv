package tv.hdonlinetv.compose.phone

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier
import tv.hdonlinetv.compose.ui.mobile.theme.PhoneTheme

/**
 * Must be [AppCompatActivity] (FragmentActivity): Cast [androidx.mediarouter.app.MediaRouteButton]
 * dialog requires it — same as legacy [tv.hdonlinetv.besttvchannels.movies.watchfree.activity.BaseActivity].
 */
class MainActivity : AppCompatActivity() {

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
