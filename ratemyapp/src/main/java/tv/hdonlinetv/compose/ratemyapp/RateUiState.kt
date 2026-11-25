package tv.hdonlinetv.compose.ratemyapp

/** What the rating UI should show. */
sealed interface RateUiState {
    /** Nothing to show. */
    data object Hidden : RateUiState

    /** Show the rating prompt (stars + Later / Never). */
    data object AskRating : RateUiState

    /**
     * Open the store page (see [rateTargetCandidates]), then call
     * [RateMyAppController.onStoreOpened].
     */
    data object OpenStore : RateUiState

    /**
     * Rating below [RateConfig.positiveThreshold] with the star filter on: thank the user and,
     * if [feedbackEmail] != null, offer an e-mail to the developer. Then call
     * [RateMyAppController.onThankYouDone].
     */
    data class ThankYou(val stars: Int, val feedbackEmail: String?) : RateUiState
}