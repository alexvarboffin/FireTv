package tv.hdonlinetv.compose.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import tv.hdonlinetv.compose.BuildConfig
import tv.hdonlinetv.compose.R

/**
 * Phone-only interstitial scaffold (legacy [tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork]).
 *
 * Disabled while [R.string.admob_interstitial_unit_id] is `"0"` / blank.
 * When enabled: DEBUG uses Google test unit; release uses the production string.
 * TV: always no-op.
 */
object InterstitialAds {

    private const val TAG = "InterstitialAds"
    /** Same default intent as legacy [tv.hdonlinetv.besttvchannels.movies.watchfree.Config.INTERSTITIAL_ADS_INTERVAL]. */
    private const val INTERVAL = 3

    private var interstitial: InterstitialAd? = null
    private var loading = false
    private var counter = 1

    fun preload(context: Context) {
        if (context.isTelevisionUi()) {
            Log.d(TAG, "preload skip: television")
            return
        }
        val unitId = resolveUnitId(context) ?: run {
            Log.d(TAG, "preload skip: unit disabled (0)")
            return
        }
        if (loading || interstitial != null) return
        loading = true
        Log.d(TAG, "loading unit=${unitId.takeLast(8)}")
        InterstitialAd.load(
            context.applicationContext,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    loading = false
                    interstitial = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            interstitial = null
                            preload(context.applicationContext)
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            Log.w(TAG, "show failed: ${adError.message}")
                            interstitial = null
                        }

                        override fun onAdShowedFullScreenContent() {
                            interstitial = null
                            Log.d(TAG, "shown")
                        }
                    }
                    Log.i(TAG, "onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    loading = false
                    interstitial = null
                    Log.w(TAG, "load failed: ${error.message}")
                }
            },
        )
    }

    /** Call before navigating to details/player from phone lists. */
    fun maybeShow(activity: Activity) {
        if (activity.isTelevisionUi()) {
            Log.d(TAG, "show skip: television")
            return
        }
        if (resolveUnitId(activity) == null) {
            Log.d(TAG, "show skip: unit disabled (0)")
            return
        }
        val ad = interstitial
        if (ad == null) {
            Log.d(TAG, "show skip: not loaded (counter=$counter)")
            preload(activity)
            return
        }
        if (counter >= INTERVAL) {
            Log.d(TAG, "showing (counter=$counter)")
            ad.show(activity)
            counter = 1
        } else {
            counter++
            Log.d(TAG, "deferred (counter=$counter/$INTERVAL)")
        }
    }

    fun maybeShowFrom(context: Context) {
        val activity = context as? Activity ?: return
        maybeShow(activity)
    }

    private fun resolveUnitId(context: Context): String? {
        val prod = context.getString(R.string.admob_interstitial_unit_id).trim()
        if (prod.isEmpty() || prod == "0") return null
        return if (BuildConfig.DEBUG) {
            context.getString(R.string.admob_interstitial_unit_id_test)
        } else {
            prod
        }
    }
}
