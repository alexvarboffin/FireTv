package tv.hdonlinetv.compose.ads

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R

private const val TAG = "AdMobBanner"

fun Context.isTelevisionUi(): Boolean {
    val uiMode = resources.configuration.uiMode and Configuration.UI_MODE_TYPE_MASK
    return uiMode == Configuration.UI_MODE_TYPE_TELEVISION
}

/**
 * Anchored adaptive banner — mirrors legacy include_banner_ad + AdNetwork
 * (container GONE until onAdLoaded). No-op on TV.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (context.isTelevisionUi()) {
        Log.d(TAG, "skip: television UI — banner not shown")
        return
    }
    val activity = context as? Activity ?: run {
        Log.w(TAG, "skip: LocalContext is not Activity")
        return
    }
    val unitId = remember {
        if (BuildConfig.DEBUG) {
            context.getString(R.string.admob_banner_unit_id_test)
        } else {
            context.getString(R.string.admob_banner_unit_id)
        }
    }
    val adView = remember {
        AdView(activity).apply {
            setAdUnitId(unitId)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
            )
        }
    }

    DisposableEffect(adView) {
        onDispose {
            Log.d(TAG, "destroy AdView unitId=$unitId")
            adView.destroy()
        }
    }

    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = {
            FrameLayout(activity).apply {
                visibility = View.GONE
                addView(adView)
                val size = anchoredAdaptiveSize(activity)
                adView.setAdSize(size)
                Log.d(TAG, "loadAd unitId=$unitId size=${size.width}x${size.height}")
                adView.adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.i(TAG, "onAdLoaded unitId=$unitId")
                        visibility = View.VISIBLE
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.e(
                            TAG,
                            "onAdFailedToLoad code=${error.code} domain=${error.domain} " +
                                "message=${error.message} cause=${error.cause} unitId=$unitId",
                        )
                        visibility = View.GONE
                        removeAllViews()
                    }

                    override fun onAdOpened() {
                        Log.d(TAG, "onAdOpened")
                    }

                    override fun onAdClosed() {
                        Log.d(TAG, "onAdClosed")
                    }

                    override fun onAdClicked() {
                        Log.d(TAG, "onAdClicked")
                    }

                    override fun onAdImpression() {
                        Log.d(TAG, "onAdImpression")
                    }
                }
                adView.loadAd(AdRequest.Builder().build())
            }
        },
    )
}

@Suppress("DEPRECATION")
private fun anchoredAdaptiveSize(activity: Activity): AdSize {
    val display = activity.windowManager.defaultDisplay
    val outMetrics = DisplayMetrics()
    display.getMetrics(outMetrics)
    val adWidth = (outMetrics.widthPixels / outMetrics.density).toInt()
    return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth)
}
