package tv.hdonlinetv.compose.ratemyapp

/**
 * When and how to ask for a rating. Pure data, no Android dependencies.
 *
 * @property minLaunches launches required before the first ask.
 * @property minDaysSinceInstall days since the first recorded launch before the first ask.
 * @property remindAfterDays days to wait after [RateMyAppController.onLater].
 * @property positiveThreshold stars (1..5) at or above which the user is sent to the store.
 * @property lowStarsFork flag for the future low-star fork (ratings below [positiveThreshold]
 *   go to an in-app feedback flow instead of just closing). Default OFF.
 *   NOT IMPLEMENTED yet: the controller ignores this flag; below-threshold ratings
 *   always end the flow with [RateUiState.Hidden].
 */
data class RateConfig(
    val minLaunches: Int = 5,
    val minDaysSinceInstall: Int = 3,
    val remindAfterDays: Int = 7,
    val positiveThreshold: Int = 4,
    val lowStarsFork: Boolean = false,
) {
    init {
        require(minLaunches >= 0) { "minLaunches < 0" }
        require(minDaysSinceInstall >= 0) { "minDaysSinceInstall < 0" }
        require(remindAfterDays >= 0) { "remindAfterDays < 0" }
        require(positiveThreshold in 1..5) { "positiveThreshold must be 1..5" }
    }
}