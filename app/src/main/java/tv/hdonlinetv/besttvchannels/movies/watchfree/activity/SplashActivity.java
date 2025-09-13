package tv.hdonlinetv.besttvchannels.movies.watchfree.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.walhalla.onboarding.OnboardingActivity;
import com.walhalla.onboarding.OnboardingManager;
import com.walhalla.ui.DLog;
import com.walhalla.ui.plugins.Module_U;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;
import tv.hdonlinetv.besttvchannels.movies.watchfree.databinding.ActivitySplashBinding;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.AdsPref;
import tv.hdonlinetv.besttvchannels.movies.watchfree.utils.PrefManager;

import java.util.Timer;
import java.util.TimerTask;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends BaseActivity {

    PrefManager prefManager;
    AdsPref adsPref;
    private ActivitySplashBinding binding;
    private OnboardingManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.textView.setText(DLog.getAppVersion(this));
        prefManager = new PrefManager(this);
        adsPref = new AdsPref(this);
        manager = new OnboardingManager(this);
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
        loadMainScreen();
    }


    private void getConfig() {
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

    private void loadMainScreen() {
        getConfig();
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                launchApp();
            }
        }, 1000);
    }

    private void launchApp() {
        if (manager.isOnboarding()) {
            Intent intent = new Intent(this, MainActivity.class).setFlags(335544320);
            startActivity(intent);
        } else {
            Intent openMainActivity = new Intent(this, OnboardingActivity.class).setFlags(335544320);
            startActivity(openMainActivity);
        }
        this.finish();
    }


    public void onResume() {
        super.onResume();
        if (prefManager.loadNightModeState() == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        initCheck();
    }

    private void initCheck() {
        if (prefManager.loadNightModeState()) {
            Log.d("Dark", "MODE");
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);   // set status text dark
        }
    }
}

