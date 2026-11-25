package tv.hdonlinetv.compose.ratemyapp

/** What the rating UI should show. */
sealed interface RateUiState {
    /** Nothing to show. */
    data object Hidden : RateUiState

    /** Show the rating prompt (stars + Later / Never). */
    data object AskRating : RateUiState

    /**
     * User gave >= [RateConfig.positiveThreshold] stars: the app should open the store page
     * (target chosen outside this controller), then call [RateMyAppController.onStoreOpened].
     */
    data object OpenStore : RateUiState
}