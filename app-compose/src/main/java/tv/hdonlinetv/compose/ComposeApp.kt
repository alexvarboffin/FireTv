package tv.hdonlinetv.compose

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import cn.jzvd.demo.CustomMedia.VlcLibHolder
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.cast.framework.CastContext
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

class ComposeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CastContext.getSharedInstance(this)
        } catch (_: Exception) {
            // Cast may be unavailable on some devices
        }
        if (isTelevisionUi()) {
            Log.d(TAG, "skip MobileAds / OneSignal on television")
        } else {
            MobileAds.initialize(this) { status ->
                Log.i(TAG, "MobileAds initialized: ${status.adapterStatusMap}")
            }
            // Same app id as legacy MyApp — phone only.
            OneSignal.Debug.logLevel = LogLevel.DEBUG
            OneSignal.initWithContext(this, ONESIGNAL_APP_ID)
            Log.i(TAG, "OneSignal init")
        }
        Thread({ VlcLibHolder.warmUp(this) }, "VlcLibHolder-warmUp").start()
    }

    private fun isTelevisionUi(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
    }

    companion object {
        private const val TAG = "ComposeAds"
        private const val ONESIGNAL_APP_ID = "e712dca5-0a2b-429a-91ce-7e6350069a18"
    }
}
