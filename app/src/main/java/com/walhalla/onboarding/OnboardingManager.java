package com.walhalla.onboarding;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;


public class OnboardingManager {

    private static final String KEY_ONBOARDING = "key_onboard009";

    private final SharedPreferences preferences;

    public OnboardingManager(Context context) {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public boolean isOnboarding() {
        return preferences.getBoolean(KEY_ONBOARDING, false);
    }

    public void isOnboarding(boolean b) {
        preferences.edit().putBoolean(KEY_ONBOARDING, b).apply();
    }
}
