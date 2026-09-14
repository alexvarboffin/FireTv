package tv.hdonlinetv.besttvchannels.movies.watchfree.utils


import android.app.Activity
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.walhalla.ui.DLog
import tv.hdonlinetv.besttvchannels.movies.watchfree.BuildConfig
import tv.hdonlinetv.besttvchannels.movies.watchfree.R

class AdNetwork(context: Activity) {
    private val mContext: Activity
    var sharedPref: SharedPref?
    var adsPref: AdsPref?

    //Banner
    private var adContainerView: FrameLayout? = null
    private var adView: AdView? = null

    //    com.facebook.ads.AdView fanAdView;
    //Interstitial
    private var adMobInterstitialAd: InterstitialAd? = null

    //    private com.facebook.ads.InterstitialAd fanInterstitialAd;
    //    private MaxInterstitialAd maxInterstitialAd;
    private val retryAttempt = 0
    private var counter = 1

    init {
        this.mContext = context
        this.sharedPref = SharedPref(context)
        this.adsPref = AdsPref(context)
    }

    fun loadBannerAdNetwork(ad_placement: Int) {
        val bannerHost: View? = mContext.findViewById(R.id.lyt_banner_ad)
        // Collapse host until a real ad is loaded (no empty strip / insets gap).
        if (bannerHost != null) {
            bannerHost.setVisibility(View.GONE)
        }
        if (adsPref.getAdStatus().equals(Constant.AD_STATUS_ON) && ad_placement != 0) {
            when (adsPref.getAdType()) {
                Constant.ADMOB -> {
                    adContainerView = mContext.findViewById(R.id.admob_banner_view_container)
                    if (adContainerView == null) {
                        return
                    }
                    adContainerView.setVisibility(View.GONE)
                    adContainerView.post({
                        adView = AdView(mContext)
                        adView.setAdUnitId(adsPref.getAdMobBannerId())
                        adContainerView.removeAllViews()
                        adContainerView.addView(adView)
                        adView.setAdSize(Tools.getAdSize(mContext))
                        adView.loadAd(Tools.getAdRequest(mContext))
                        adView.setAdListener(object : AdListener() {
                            @Override
                            fun onAdLoaded() {
                                // Code to be executed when an ad finishes loading.
                                adContainerView.setVisibility(View.VISIBLE)
                                if (bannerHost != null) {
                                    bannerHost.setVisibility(View.VISIBLE)
                                }
                            }

                            @Override
                            fun onAdFailedToLoad(@NonNull error: LoadAdError) {
                                if (BuildConfig.DEBUG) {
                                    var errorReason = ""
                                    val code: Int = error.getCode()
                                    if (code == AdRequest.ERROR_CODE_INTERNAL_ERROR) {
                                        errorReason = "Internal error"
                                    } else if (code == AdRequest.ERROR_CODE_INVALID_REQUEST) {
                                        errorReason = "Invalid request"
                                    } else if (code == AdRequest.ERROR_CODE_NETWORK_ERROR) {
                                        errorReason = "Network Error"
                                        /*
                                         * The ad request was successful, but no ad was returned due to lack of ad inventory.
                                         * */
                                    } else if (code == AdRequest.ERROR_CODE_NO_FILL) {
                                        errorReason = "No fill"

                                        //                PACKAGE_NAME_KEY_LEGACY_VISIBLE}, //#{@link #PACKAGE_NAME_KEY_LEGACY_NOT_VISIBLE
//                case VISIBILITY_UNDEFINED:
//                    errorReason = "onAdFailedToLoad: VISIBILITY_UNDEFINED";
                                    }

                                    DLog.d(
                                        String.format(
                                            "Ad %s failed to load with error %s.",
                                            adView.getAdUnitId(),
                                            errorReason
                                        )
                                    )

                                    if (error.getCode() === AdRequest.ERROR_CODE_NETWORK_ERROR) {
//                                            if (((AdView) mObject).getVisibility() == View.VISIBLE) {
//                                                ((AdView) mObject).setVisibility(View.GONE);
//                                            }
                                    }
                                }
                                // Collapse entire banner host — not only the inner AdMob container
                                // (outer lyt_banner_ad still held bottom inset margin otherwise).
                                adContainerView.setVisibility(View.GONE)
                                adContainerView.removeAllViews()
                                if (bannerHost != null) {
                                    bannerHost.setVisibility(View.GONE)
                                }
                            }

                            @Override
                            fun onAdOpened() {
                                // Code to be executed when an ad opens an overlay that
                                // covers the screen.
                            }

                            @Override
                            fun onAdClicked() {
                                // Code to be executed when the user clicks on an ad.
                            }

                            @Override
                            fun onAdClosed() {
                                // Code to be executed when the user is about to return
                                // to the app after tapping on an ad.
                            }
                        })
                    })
                }
            }
        }
    }

    fun loadInterstitialAdNetwork(ad_placement: Int) {
        if (adsPref.getAdStatus().equals(Constant.AD_STATUS_ON) && ad_placement != 0) {
            when (adsPref.getAdType()) {
                Constant.ADMOB -> InterstitialAd.load(
                    mContext,
                    adsPref.getAdMobInterstitialId(),
                    Tools.getAdRequest(mContext),
                    object : InterstitialAdLoadCallback() {
                        @Override
                        fun onAdLoaded(@NonNull interstitialAd: InterstitialAd?) {
                            adMobInterstitialAd = interstitialAd
                            adMobInterstitialAd.setFullScreenContentCallback(object :
                                FullScreenContentCallback() {
                                @Override
                                fun onAdDismissedFullScreenContent() {
                                    loadInterstitialAdNetwork(ad_placement)
                                }

                                @Override
                                fun onAdFailedToShowFullScreenContent(@NonNull adError: com.google.android.gms.ads.AdError?) {
                                    Log.d(
                                        tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork.Companion.TAG,
                                        "The ad failed to show."
                                    )
                                }

                                @Override
                                fun onAdShowedFullScreenContent() {
                                    adMobInterstitialAd = null
                                    Log.d(
                                        tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork.Companion.TAG,
                                        "The ad was shown."
                                    )
                                }
                            })
                            Log.i(
                                tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork.Companion.TAG,
                                "onAdLoaded"
                            )
                        }

                        @Override
                        fun onAdFailedToLoad(@NonNull loadAdError: LoadAdError) {
                            Log.i(
                                tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork.Companion.TAG,
                                loadAdError.getMessage()
                            )
                            adMobInterstitialAd = null
                            Log.d(
                                tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdNetwork.Companion.TAG,
                                "Failed load AdMob Interstitial Ad"
                            )
                        }
                    })

            }
        }
    }

    fun showInterstitialAdNetwork(ad_placement: Int, interval: Int) {
        if (adsPref.getAdStatus().equals(Constant.AD_STATUS_ON) && ad_placement != 0) {
            when (adsPref.getAdType()) {
                Constant.ADMOB -> if (adMobInterstitialAd != null) {
                    if (counter == interval) {
                        adMobInterstitialAd.show(mContext)
                        counter = 1
                    } else {
                        counter++
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = "AdNetwork"
    }
}
