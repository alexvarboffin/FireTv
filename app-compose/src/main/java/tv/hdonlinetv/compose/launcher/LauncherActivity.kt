package tv.hdonlinetv.compose.launcher

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.MainActivity as PhoneMainActivity
import tv.hdonlinetv.compose.tv.MainActivity as TvMainActivity
import tv.hdonlinetv.compose.ui.mobile.theme.NightModeApplier

class LauncherActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val settingsRepository = LocalSettingsRepository(applicationContext)
        NightModeApplier.apply(settingsRepository.getSettings().nightMode)
        super.onCreate(savedInstanceState)
        val isTv = (resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION
            //|| BuildConfig.DEBUG
        val target = if (isTv) TvMainActivity::class.java else PhoneMainActivity::class.java
        startActivity(Intent(this, target))
        finish()
    }
}
