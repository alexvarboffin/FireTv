package tv.hdonlinetv.compose.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/** GDPR / UMP consent (legacy `utils/GDPR`). Phone only — TV runs without ads. */
object AdsConsent {

    private const val TAG = "AdsConsent"

    fun gather(activity: Activity) {
        if (activity.isTelevisionUi()) {
            Log.d(TAG, "skip on television")
            return
        }
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                Log.d(TAG, "info updated: status=${statusName(consentInformation.consentStatus)}")
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "form error ${formError.errorCode}: ${formError.message}")
                    }
                    Log.d(
                        TAG,
                        "form done: status=${statusName(consentInformation.consentStatus)} " +
                            "canRequestAds=${consentInformation.canRequestAds()}",
                    )
                }
            },
            { requestError ->
                Log.w(TAG, "info update failed ${requestError.errorCode}: ${requestError.message}")
            },
        )
    }

    private fun statusName(status: Int): String = when (status) {
        ConsentInformation.ConsentStatus.REQUIRED -> "REQUIRED"
        ConsentInformation.ConsentStatus.NOT_REQUIRED -> "NOT_REQUIRED"
        ConsentInformation.ConsentStatus.OBTAINED -> "OBTAINED"
        else -> "UNKNOWN"
    }
}
