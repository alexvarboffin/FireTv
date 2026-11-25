package tv.hdonlinetv.compose.ratemyapp

/**
 * When and how to ask for a rating. Pure data, no Android dependencies.
 * Defaults mirror the legacy WalhallaUI rate-me flow used by FireTv :app
 * (com.walhalla.ui.observer.RateAppModule + com.androidsx.rateme.RateMeDialog).
 *
 * @property minLaunches launches before the prompt can be armed (legacy LAUNCHES_UNTIL_PROMPT_ = 5).
 * @property pauseStepsMinutes pause before each ask, counted from the moment the prompt was armed
 *   (minLaunches reached) or from the last [RateMyAppController.onLater]. Index = number of Later
 *   presses so far. When Later is pressed on the last step, the flow ends (never ask again).
 *   Legacy LEVEL_1_/2_/3_ = 60/120/180 min.
 * @property minDaysSinceInstall extra gate in days since the first recorded launch. Legacy declares
 *   DAYS_UNTIL_PROMPT = 1.0 but never uses it, so the effective legacy value is 0.
 * @property showDelayMillis hint for the UI: delay before showing the prompt once it becomes due
 *   (legacy handler.postDelayed(runnable, 2000)). The controller does not use it.
 * @property positiveThreshold stars (1..5) at or above which the user goes to the store
 *   (legacy RATING_LIMIT = 5.0).
 * @property starFilterEnabled true: >= [positiveThreshold] -> [RateUiState.OpenStore],
 *   below -> [RateUiState.ThankYou] (UI offers e-mail feedback). false: any rating -> OpenStore.
 * @property feedbackEmail developer address for low-rating feedback, null = no e-mail offer.
 *   The app passes it in; no address is stored in this module.
 * @property showOnTv false: never ask on TV devices (see [RateMyAppController] isTv input).
 */
data class RateConfig(
    val minLaunches: Int = 5,
    val pauseStepsMinutes: List<Int> = listOf(60, 120, 180),
    val minDaysSinceInstall: Int = 0,
    val showDelayMillis: Long = 2_000L,
    val positiveThreshold: Int = 5,
    val starFilterEnabled: Boolean = true,
    val feedbackEmail: String? = null,
    val showOnTv: Boolean = false,
) {
    init {
        require(minLaunches >= 0) { "minLaunches < 0" }
        require(pauseStepsMinutes.isNotEmpty() && pauseStepsMinutes.all { it >= 0 }) {
            "pauseStepsMinutes must be non-empty and >= 0"
        }
        require(minDaysSinceInstall >= 0) { "minDaysSinceInstall < 0" }
        require(showDelayMillis >= 0) { "showDelayMillis < 0" }
        require(positiveThreshold in 1..5) { "positiveThreshold must be 1..5" }
    }
}