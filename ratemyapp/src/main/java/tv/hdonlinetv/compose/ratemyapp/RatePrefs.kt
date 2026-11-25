package tv.hdonlinetv.compose.ratemyapp

/** Persistent counters of the rating flow. Implementations must persist writes. */
interface RatePrefs {
    var launchCount: Int

    /** Epoch millis of the first recorded launch, 0 = not recorded yet. */
    var firstLaunchMillis: Long

    /** Epoch millis the current pause step counts from (armed / last Later), 0 = not armed. */
    var anchorMillis: Long

    /** Number of Later presses = index into [RateConfig.pauseStepsMinutes]. */
    var laterCount: Int

    /** True after the user rated, pressed Never or used up all pause steps: never ask again. */
    var completed: Boolean
}