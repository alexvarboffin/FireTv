package tv.hdonlinetv.besttvchannels.movies.watchfree.utils;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import tv.hdonlinetv.besttvchannels.movies.watchfree.R;

import com.google.android.ump.ConsentForm;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.FormError;
import com.google.android.ump.UserMessagingPlatform;
import com.walhalla.ui.DLog;

import java.net.MalformedURLException;
import java.net.URL;

@Keep
public class GDPR {

    private ConsentInformation consentInformation;
    private ConsentForm mConsentForm;

    public GDPR() {
    }

    public void init(Activity activity) {
//        ConsentDebugSettings debugSettings = new ConsentDebugSettings.Builder(activity)
//                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
//                .addTestDeviceHashedId("443D3BC0E32E203FC1EA78DAD213A128")
//                .addTestDeviceHashedId("8808EDEFBED3C7B6955EAA2FEF5417E0")
//                .build();


//        ConsentRequestParameters params = new ConsentRequestParameters
//                .Builder()
//                .setConsentDebugSettings(debugSettings)
//                .build();


        // Set tag for under age of consent. false means users are not under
        // age.
        ConsentRequestParameters params = new ConsentRequestParameters
                .Builder()
                .setTagForUnderAgeOfConsent(false)
                .build();


        consentInformation = UserMessagingPlatform.getConsentInformation(activity);
        //@@@@@@@@@@@
        //consentInformation.reset();
        consentInformation.requestConsentInfoUpdate(
                activity,
                params, () -> {
                    // The consent information state was updated.
                    // You are now ready to check if a form is available.
                    //DLog.d("@isConsentFormAvailable@" + consentInformation.isConsentFormAvailable());
                    if (consentInformation.isConsentFormAvailable()) {
                        loadForm(activity);
                    }

                },
                new ConsentInformation.OnConsentInfoUpdateFailureListener() {
                    @Override
                    public void onConsentInfoUpdateFailure(@NonNull FormError formError) {
                        DLog.d("@@" + formError.getMessage());
                    }
                });
    }


    private void loadForm(Activity activity) {
        // Loads a consent form. Must be called on the main thread.
        UserMessagingPlatform.loadConsentForm(
                activity,
                new UserMessagingPlatform.OnConsentFormLoadSuccessListener() {
                    @Override
                    public void onConsentFormLoadSuccess(@NonNull ConsentForm consentForm) {
                        mConsentForm = consentForm;
                        if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.REQUIRED) {
                            mConsentForm.show(
                                    activity,
                                    new ConsentForm.OnConsentFormDismissedListener() {
                                        @Override
                                        public void onConsentFormDismissed(@Nullable FormError formError) {
                                            if (consentInformation.getConsentStatus() == ConsentInformation.ConsentStatus.OBTAINED) {
                                                // App can start requesting ads.
                                            }

                                            // Handle dismissal by reloading form.
                                            loadForm(activity);
                                        }
                                    });

                        }
                    }
                },
                new UserMessagingPlatform.OnConsentFormLoadFailureListener() {
                    @Override
                    public void onConsentFormLoadFailure(@NonNull FormError formError) {
                        // Handle the error.
                    }
                }
        );
    }
}
