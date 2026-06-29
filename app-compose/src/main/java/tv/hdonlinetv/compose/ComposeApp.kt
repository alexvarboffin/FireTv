package tv.hdonlinetv.compose

import android.app.Application
import cn.jzvd.demo.CustomMedia.VlcLibHolder
import com.google.android.gms.cast.framework.CastContext

class ComposeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            CastContext.getSharedInstance(this)
        } catch (_: Exception) {
            // Cast may be unavailable on some devices
        }
        Thread({ VlcLibHolder.warmUp(this) }, "VlcLibHolder-warmUp").start()
    }
}
