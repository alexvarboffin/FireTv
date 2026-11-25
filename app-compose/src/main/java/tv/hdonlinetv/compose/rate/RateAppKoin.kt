package tv.hdonlinetv.compose.rate

import org.koin.dsl.module
import tv.hdonlinetv.compose.ratemyapp.RateConfig

/**
 * App-side override of :ratemyapp's `single { RateConfig() }`: load AFTER ratemyappModule.
 * Only the feedback e-mail differs from the module defaults.
 */
val rateAppConfigModule = module {
    single { RateConfig(feedbackEmail = RATE_FEEDBACK_EMAIL) }
}