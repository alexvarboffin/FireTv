package tv.hdonlinetv.compose.tv

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier
import tv.hdonlinetv.compose.ui.tv.theme.TvTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val nightMode = LocalSettingsRepository(newBase.applicationContext ?: newBase)
            .getSettings().nightMode
        NightModeApplier.apply(nightMode)
        super.attachBaseContext(NightModeApplier.wrap(newBase, nightMode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            // Phone debug of TV UI — force landscape like a TV.
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        val nightMode = LocalSettingsRepository(applicationContext).getSettings().nightMode
        NightModeApplier.apply(nightMode)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TvTheme(isDarkTheme = nightMode) {
                TvNavHost()
            }
        }
    }
}
