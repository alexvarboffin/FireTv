package tv.hdonlinetv.besttvchannels.movies.watchfree.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import com.walhalla.onboarding.OnboardingActivity
import com.walhalla.onboarding.OnboardingManager
import com.walhalla.ui.DLog.getAppVersion
import tv.hdonlinetv.besttvchannels.movies.watchfree.activity.MainActivity
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivitySplashBinding
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager
import java.util.Timer
import java.util.TimerTask


@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity() {
    var prefManager: PrefManager? = null
    var adsPref: AdsPref? = null
    private var binding: ActivitySplashBinding? = null
    private var manager: OnboardingManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(
            layoutInflater
        )
        setContentView(binding!!.root)
        binding!!.textView.text = getAppVersion(this)
        prefManager = PrefManager(this)
        adsPref = AdsPref(this)
        manager = OnboardingManager(this)
//        try {
//            YoYo.with(Techniques.RotateInDownLeft)
//                    .duration(1500)
//                    .repeat(0)
//                    .playOn(binding.imageView1);
//
//            YoYo.with(Techniques.RotateInDownRight)
//                    .duration(1500)
//                    .repeat(0)
//                    .playOn(binding.imageView2);
//        } catch (Exception ex) {
//            DLog.handleException(ex);
//        }
        //getData();
        loadMainScreen()
    }


    private val config: Unit
        get() {
//        adsPref.saveAds(
//                AD_STATUS,
//                AD_TYPE,
//                ADMOB_PUB_ID,
//                ADMOB_PUB_ID,
//                BANNER_ID,
//                INTER_ID,
//                DEVELOPER_NAME,
//                DEVELOPER_NAME,
//                FACEBOOK_BANNER_ID,
//                FACEBOOK_INTER_ID,
//                DEVELOPER_NAME,
//                DEVELOPER_NAME,
//                DEVELOPER_NAME,
//                DEVELOPER_NAME,
//                DEVELOPER_NAME,
//                APPLOVIN_BANNER_ID,
//                APPLOVIN_INTER_ID,
//                INTERSTITIAL_ADS_INTERVAL,
//                INTERSTITIAL_ADS_INTERVAL,
//                INTERSTITIAL_ADS_INTERVAL,
//                prefManager.getString("VDN"),
//                ""
//        );
        }

    //    private void getData() {
    //        DLog.d("@@@@" + Constant.URL_API_DATA);
    //        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET, Constant.URL_API_DATA, null, new Response.Listener<JSONObject>() {
    //            @Override
    //            public void onResponse(JSONObject response) {
    //
    //                try {
    //                    final String VDN = response.getString("DN");
    //                    prefManager.setString("VDN", VDN);
    //                    getConfig();
    //                    Timer myTimer = new Timer();
    //                    myTimer.schedule(new TimerTask() {
    //                        @Override
    //                        public void run() {
    //                            // If you want to modify a view in your Activity
    //                            SplashActivity.this.runOnUiThread(() -> {
    //                                startActivity(new Intent(SplashActivity.this, MainActivity.class));
    //                                finish();
    //
    //                            });
    //                        }
    //                    }, 1000);
    //
    //                } catch (JSONException e) {
    //                    e.printStackTrace();
    //                }
    //            }
    //        }, new Response.ErrorListener() {
    //
    //            @Override
    //            public void onErrorResponse(VolleyError error) {
    //
    //            }
    //        });
    //
    //        MyApp.getInstance().addToRequestQueue(jsonObjReq);
    //    }
    private fun loadMainScreen() {
        config
        val myTimer = Timer()
        myTimer.schedule(object : TimerTask() {
            override fun run() {
                launchApp()
            }
        }, 1000)
    }

    private fun launchApp() {
        if (manager!!.isOnboarding) {
            val intent = Intent(
                this,
                MainActivity::class.java
            ).setFlags(335544320)
            startActivity(intent)
        } else {
            val openMainActivity = Intent(
                this,
                OnboardingActivity::class.java
            ).setFlags(335544320)
            startActivity(openMainActivity)
        }
        this.finish()
    }


    public override fun onResume() {
        super.onResume()
        if (prefManager!!.loadNightModeState() == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        initCheck()
    }

    private fun initCheck() {
        if (prefManager!!.loadNightModeState()) {
            Log.d("Dark", "MODE")
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR // set status text dark
        }
    }
}

