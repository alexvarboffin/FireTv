package tv.hdonlinetv.compose.tv

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.ui.tv.theme.TvTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        if (BuildConfig.DEBUG) {
            // Phone debug of TV UI — force landscape like a TV.
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TvTheme {
                TvNavHost()
            }
        }
    }
}
