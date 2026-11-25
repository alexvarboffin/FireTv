package tv.hdonlinetv.compose.rate

import java.net.URLEncoder

/**
 * UTV-R6c: developer address for low-rating feedback. Same address as the legacy :app rate flow
 * (WalhallaUI com.walhalla.ui.observer.RateAppModule.v0 -> RateMeDialog.enableFeedbackByEmail).
 * Lives in app-compose on purpose: :ratemyapp keeps RateConfig.feedbackEmail = null by default.
 */
const val RATE_FEEDBACK_EMAIL: String = "alexvarboffin@gmail.com"

/**
 * mailto: URI for ACTION_SENDTO with an RFC 6068 encoded subject (spaces as %20, not '+').
 * Pure JVM so it is unit-testable without Android.
 */
fun feedbackMailUri(email: String, subject: String): String {
    val encodedSubject = URLEncoder.encode(subject, Charsets.UTF_8.name()).replace("+", "%20")
    return "mailto:${email.trim()}?subject=$encodedSubject"
}