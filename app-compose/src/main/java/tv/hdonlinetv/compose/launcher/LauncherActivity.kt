package tv.hdonlinetv.compose.launcher

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import org.koin.android.ext.android.get
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.MainActivity as PhoneMainActivity
import tv.hdonlinetv.compose.rate.isLauncherReentry
import tv.hdonlinetv.compose.rate.shouldCountLaunch
import tv.hdonlinetv.compose.ratemyapp.RateMyAppController
import tv.hdonlinetv.compose.tv.MainActivity as TvMainActivity
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {


        println("@@@ www${Build.MODEL}")


        val settingsRepository = LocalSettingsRepository(applicationContext)
        NightModeApplier.apply(settingsRepository.getSettings().nightMode)
        super.onCreate(savedInstanceState)
        // Launcher re-entry (icon / OneSignal tap while the app task is alive): the task already
        // shows MainActivity, so just drop this instance and let the existing task come forward.
        if (isLauncherReentry(isTaskRoot, intent?.action, intent?.categories)) {
            finish()
            return
        }
        // UTV-R6c: one rate-me launch per user start of the app (phone + TV share this launcher).
        if (shouldCountLaunch(isRestored = savedInstanceState != null, isTaskRoot = isTaskRoot)) {
            get<RateMyAppController>().onAppLaunched()
        }
        val isTv = (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
                    || (BuildConfig.DEBUG && "SM-S906N" == Build.MODEL)
        val target = if (isTv) TvMainActivity::class.java else PhoneMainActivity::class.java
        // Reuse a live MainActivity in this task instead of stacking a second one.
        startActivity(Intent(this, target).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
        finish()
    }
}
