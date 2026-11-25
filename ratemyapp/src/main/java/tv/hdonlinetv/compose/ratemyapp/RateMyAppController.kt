package tv.hdonlinetv.compose.ratemyapp

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Decides when to ask for a rating and what happens after the answer. No UI, no Intents. */
interface RateMyAppController {
    val state: StateFlow<RateUiState>

    /** Call once per app start (e.g. from Activity.onCreate with savedInstanceState == null). */
    fun onAppLaunched()

    /** True if the prompt is due now (thresholds met, not snoozed, not completed). */
    fun shouldShow(): Boolean

    /** User picked [stars] (1..5). Ends the flow; >= threshold -> [RateUiState.OpenStore]. */
    fun onRated(stars: Int)

    /** Snooze for [RateConfig.remindAfterDays]. */
    fun onLater()

    /** Never ask again. */
    fun onNever()

    /** The app handled [RateUiState.OpenStore] (store opened or failed): back to Hidden. */
    fun onStoreOpened()
}

class DefaultRateMyAppController(
    private val config: RateConfig,
    private val prefs: RatePrefs,
    private val now: () -> Long = System::currentTimeMillis,
) : RateMyAppController {

    private val _state = MutableStateFlow<RateUiState>(RateUiState.Hidden)
    override val state: StateFlow<RateUiState> = _state.asStateFlow()

    override fun onAppLaunched() {
        if (prefs.firstLaunchMillis == 0L) prefs.firstLaunchMillis = now()
        prefs.launchCount = prefs.launchCount + 1
        if (shouldShow()) _state.value = RateUiState.AskRating
    }

    override fun shouldShow(): Boolean {
        if (prefs.completed) return false
        if (prefs.launchCount < config.minLaunches) return false
        val first = prefs.firstLaunchMillis
        if (first == 0L) return false
        val t = now()
        if (t - first < config.minDaysSinceInstall * DAY_MS) return false
        val remindAt = prefs.remindAtMillis
        return remindAt == 0L || t >= remindAt
    }

    override fun onRated(stars: Int) {
        require(stars in 1..5) { "stars must be 1..5, was $stars" }
        prefs.completed = true
        // config.lowStarsFork is intentionally not used yet (see RateConfig KDoc).
        _state.value = if (stars >= config.positiveThreshold) RateUiState.OpenStore else RateUiState.Hidden
    }

    override fun onLater() {
        prefs.remindAtMillis = now() + config.remindAfterDays * DAY_MS
        _state.value = RateUiState.Hidden
    }

    override fun onNever() {
        prefs.completed = true
        _state.value = RateUiState.Hidden
    }

    override fun onStoreOpened() {
        _state.value = RateUiState.Hidden
    }

    private companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000
    }
}