package tv.hdonlinetv.compose

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import cn.jzvd.demo.CustomMedia.VlcLibHolder
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.cast.framework.CastContext

class ComposeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CastContext.getSharedInstance(this)
        } catch (_: Exception) {
            // Cast may be unavailable on some devices
        }
        if (isTelevisionUi()) {
            Log.d(TAG, "MobileAds: skip initialize on television")
        } else {
            MobileAds.initialize(this) { status ->
                Log.i(TAG, "MobileAds initialized: ${status.adapterStatusMap}")
            }
        }
        Thread({ VlcLibHolder.warmUp(this) }, "VlcLibHolder-warmUp").start()
    }

    private fun isTelevisionUi(): Boolean {
        val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
        return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
    }

    companion object {
        private const val TAG = "ComposeAds"
    }
}
