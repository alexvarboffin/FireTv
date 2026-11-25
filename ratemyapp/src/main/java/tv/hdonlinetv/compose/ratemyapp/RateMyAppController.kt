package tv.hdonlinetv.compose.ratemyapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Decides when to ask for a rating and what happens after the answer. No UI, no Intents. */
interface RateMyAppController {
    val state: StateFlow<RateUiState>

    /** Call once per app start (e.g. Activity.onCreate with savedInstanceState == null). */
    fun onAppLaunched()

    /** True if the prompt is due now. Always false on TV unless [RateConfig.showOnTv]. */
    fun shouldShow(): Boolean

    /** User picked [stars] (1..5). Ends the flow: [RateUiState.OpenStore] or [RateUiState.ThankYou]. */
    fun onRated(stars: Int)

    /** Snooze to the next pause step; on the last step ends the flow. */
    fun onLater()

    /** Never ask again. */
    fun onNever()

    /** The app handled [RateUiState.OpenStore] (store opened or failed): back to Hidden. */
    fun onStoreOpened()

    /** The app finished [RateUiState.ThankYou] (e-mail sent or skipped): back to Hidden. */
    fun onThankYouDone()
}

class DefaultRateMyAppController(
    private val config: RateConfig,
    private val prefs: RatePrefs,
    private val isTv: () -> Boolean,
    private val now: () -> Long = System::currentTimeMillis,
) : RateMyAppController {

    private val _state = MutableStateFlow<RateUiState>(RateUiState.Hidden)
    override val state: StateFlow<RateUiState> = _state.asStateFlow()

    override fun onAppLaunched() {
        val t = now()
        if (prefs.firstLaunchMillis == 0L) prefs.firstLaunchMillis = t
        prefs.launchCount = prefs.launchCount + 1
        // Legacy: the pause starts when the launch threshold is first reached.
        if (prefs.launchCount >= config.minLaunches && prefs.anchorMillis == 0L) prefs.anchorMillis = t
        if (shouldShow()) _state.value = RateUiState.AskRating
    }

    override fun shouldShow(): Boolean {
        if (!config.showOnTv && isTv()) return false
        if (prefs.completed) return false
        if (prefs.launchCount < config.minLaunches) return false
        val anchor = prefs.anchorMillis
        if (anchor == 0L) return false
        val t = now()
        if (t - prefs.firstLaunchMillis < config.minDaysSinceInstall * DAY_MS) return false
        val step = prefs.laterCount.coerceIn(0, config.pauseStepsMinutes.lastIndex)
        return t - anchor >= config.pauseStepsMinutes[step] * MINUTE_MS
    }

    override fun onRated(stars: Int) {
        require(stars in 1..5) { "stars must be 1..5, was $stars" }
        prefs.completed = true
        _state.value = if (!config.starFilterEnabled || stars >= config.positiveThreshold) {
            RateUiState.OpenStore
        } else {
            RateUiState.ThankYou(stars, config.feedbackEmail)
        }
    }

    override fun onLater() {
        val next = prefs.laterCount + 1
        prefs.laterCount = next
        if (next >= config.pauseStepsMinutes.size) prefs.completed = true else prefs.anchorMillis = now()
        _state.value = RateUiState.Hidden
    }

    override fun onNever() {
        prefs.completed = true
        _state.value = RateUiState.Hidden
    }

    override fun onStoreOpened() {
        _state.value = RateUiState.Hidden
    }

    override fun onThankYouDone() {
        _state.value = RateUiState.Hidden
    }

    private companion object {
        const val MINUTE_MS = 60L * 1000
        const val DAY_MS = 24L * 60 * MINUTE_MS
    }
}