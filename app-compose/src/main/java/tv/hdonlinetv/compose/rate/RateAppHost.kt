package tv.hdonlinetv.compose.rate

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import kotlinx.coroutines.delay
import org.koin.core.context.GlobalContext
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.ratemyapp.RateConfig
import tv.hdonlinetv.compose.ratemyapp.RateMyAppController
import tv.hdonlinetv.compose.ratemyapp.RateUiState
import tv.hdonlinetv.compose.ui.components.RateAppDialog
import tv.hdonlinetv.compose.ui.components.RateThankYouDialog
import tv.hdonlinetv.compose.util.startActivitySafe

/** ACTION_SENDTO mail to the developer (address + subject also as extras for picky clients). */
fun feedbackMailIntent(email: String, subject: String): Intent =
    Intent(Intent.ACTION_SENDTO, feedbackMailUri(email, subject).toUri())
        .putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
        .putExtra(Intent.EXTRA_SUBJECT, subject)

private fun Context.sendRateFeedback(email: String, appName: String) {
    startActivitySafe(feedbackMailIntent(email, getString(R.string.rate_feedback_subject, appName)))
}

/**
 * UTV-R6c: renders [RateMyAppController.state] (mobile + TV). Hosted next to MainShellScreen.
 * TV gating (RateConfig.showOnTv) is done by the controller; Hidden renders nothing.
 */
@Composable
fun RateAppHost(
    controller: RateMyAppController = remember { GlobalContext.get().get() },
    config: RateConfig = remember { GlobalContext.get().get() },
) {
    val state by controller.state.collectAsState()
    val context = LocalContext.current
    val appName = stringResource(R.string.app_name)

    when (val s = state) {
        RateUiState.Hidden -> Unit
        RateUiState.AskRating -> {
            // Legacy RateAppModule posts the prompt with a 2 s delay (RateConfig.showDelayMillis).
            var ready by remember { mutableStateOf(config.showDelayMillis <= 0L) }
            LaunchedEffect(Unit) {
                delay(config.showDelayMillis)
                ready = true
            }
            var stars by rememberSaveable { mutableIntStateOf(0) }
            RateAppDialog(
                visible = ready,
                appName = appName,
                selectedStars = stars,
                onStarsChange = { stars = it },
                onRate = { controller.onRated(it) },
                onLater = { controller.onLater() },
            )
        }
        is RateUiState.ThankYou -> RateThankYouDialog(
            visible = true,
            stars = s.stars,
            feedbackEmail = s.feedbackEmail,
            onWriteToDeveloper = {
                s.feedbackEmail?.takeIf { it.isNotBlank() }?.let { context.sendRateFeedback(it, appName) }
                controller.onThankYouDone()
            },
            onDone = { controller.onThankYouDone() },
        )
        RateUiState.OpenStore -> LaunchedEffect(Unit) {
            context.openRateStore()
            controller.onStoreOpened()
        }
    }
}