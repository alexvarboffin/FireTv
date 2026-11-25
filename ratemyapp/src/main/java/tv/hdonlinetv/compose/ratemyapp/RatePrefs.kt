package tv.hdonlinetv.compose.ratemyapp

/** Persistent counters of the rating flow. Implementations must persist writes. */
interface RatePrefs {
    var launchCount: Int

    /** Epoch millis of the first recorded launch, 0 = not recorded yet. */
    var firstLaunchMillis: Long

    /** Epoch millis before which the prompt is snoozed (after Later), 0 = no snooze. */
    var remindAtMillis: Long

    /** True after the user rated or pressed Never: never ask again. */
    var completed: Boolean
}