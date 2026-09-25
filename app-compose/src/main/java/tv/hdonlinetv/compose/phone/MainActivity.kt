package tv.hdonlinetv.compose.phone

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import tv.hdonlinetv.compose.ads.AdsConsent
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.player.CustomMediaRouteButton
import tv.hdonlinetv.compose.player.MediaRouteButtonManager
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier
import tv.hdonlinetv.compose.ui.mobile.theme.PhoneTheme

/**
 * Cinema Cast livehack: [FragmentActivity] (not ComponentActivity) + Theme.AppCompat activity theme
 * + hidden [CustomMediaRouteButton] kept attached to the window. Compose Cast icon calls
 * [MediaRouteButtonManager.current]`.performClick()`.
 */
class MainActivity : FragmentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val nightMode = LocalSettingsRepository(newBase.applicationContext ?: newBase)
            .getSettings().nightMode
        NightModeApplier.apply(nightMode)
        super.attachBaseContext(NightModeApplier.wrap(newBase, nightMode))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsRepository = LocalSettingsRepository(applicationContext)
        val nightMode = settingsRepository.getSettings().nightMode
        NightModeApplier.apply(nightMode)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mediaRouteButton = try {
            val button = CustomMediaRouteButton(this).apply {
                visibility = View.GONE
            }
            CastContext.getSharedInstance(this)
            CastButtonFactory.setUpMediaRouteButton(applicationContext, button)
            button
        } catch (_: Exception) {
            null
        }

        setContent {
            PhoneTheme(darkTheme = nightMode) {
                Box(Modifier.fillMaxSize()) {
                    mediaRouteButton?.let { mRButton ->
                        // Must stay attached to the window (Cinema). onReset non-null avoids
                        // Compose recycling that leaves the activity unresponsive after background.
                        AndroidView(
                            factory = { mRButton },
                            modifier = Modifier.size(0.dp),
                            onRelease = { MediaRouteButtonManager.remove(it) },
                            update = { updated: CustomMediaRouteButton ->
                                if (updated.isAttachedToWindow) {
                                    MediaRouteButtonManager.add(updated)
                                }
                            },
                            onReset = {},
                        )
                    }
                    PhoneNavHost()
                }
            }
        }
        AdsConsent.gather(this)
    }
}
