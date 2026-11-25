package tv.hdonlinetv.compose.ratemyapp

import android.content.Context
import android.content.SharedPreferences

/** [RatePrefs] stored in a private SharedPreferences file. */
class SharedPreferencesRatePrefs(
    private val prefs: SharedPreferences,
) : RatePrefs {

    constructor(context: Context) : this(
        context.applicationContext.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
    )

    override var launchCount: Int
        get() = prefs.getInt(KEY_LAUNCHES, 0)
        set(value) = prefs.edit().putInt(KEY_LAUNCHES, value).apply()

    override var firstLaunchMillis: Long
        get() = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        set(value) = prefs.edit().putLong(KEY_FIRST_LAUNCH, value).apply()

    override var remindAtMillis: Long
        get() = prefs.getLong(KEY_REMIND_AT, 0L)
        set(value) = prefs.edit().putLong(KEY_REMIND_AT, value).apply()

    override var completed: Boolean
        get() = prefs.getBoolean(KEY_COMPLETED, false)
        set(value) = prefs.edit().putBoolean(KEY_COMPLETED, value).apply()

    companion object {
        const val FILE_NAME = "ratemyapp"
        private const val KEY_LAUNCHES = "launch_count"
        private const val KEY_FIRST_LAUNCH = "first_launch_ms"
        private const val KEY_REMIND_AT = "remind_at_ms"
        private const val KEY_COMPLETED = "completed"
    }
}