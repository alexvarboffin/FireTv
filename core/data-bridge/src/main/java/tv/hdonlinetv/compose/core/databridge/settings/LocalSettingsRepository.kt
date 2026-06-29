package tv.hdonlinetv.compose.core.databridge.settings

import android.content.Context
import tv.hdonlinetv.compose.core.domain.model.AppSettingsUi
import tv.hdonlinetv.compose.core.domain.repository.SettingsRepository

class LocalSettingsRepository(
    context: Context,
) : SettingsRepository {

    private val prefs = context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    override fun getSettings(): AppSettingsUi = AppSettingsUi(
        gridColumns = prefs.getInt(KEY_CHANNEL_COLUMNS, DEFAULT_COLUMNS),
        sortOption = prefs.getInt(PREF_SORT_KEY, 0),
        detailsMode = prefs.getBoolean(KEY_ACTIVITY_MODE, false),
        isFirstLaunch = prefs.getBoolean(IS_FIRST_TIME_LAUNCH, true),
        nightMode = prefs.getBoolean(KEY_NIGHT_MODE, false),
        mediaPlayerOption = prefs.getInt(KEY_MEDIA_PLAYER, DEFAULT_MEDIA_PLAYER),
    )

    override fun setGridColumns(columns: Int) {
        prefs.edit()
            .putInt(KEY_CHANNEL_COLUMNS, columns)
            .putString(KEY_COL_COUNT, if (columns > 1) TYPE_GRID else TYPE_LIST)
            .apply()
    }

    override fun setSortOption(option: Int) {
        prefs.edit().putInt(PREF_SORT_KEY, option).apply()
    }

    override fun setDetailsMode(detailsMode: Boolean) {
        prefs.edit().putBoolean(KEY_ACTIVITY_MODE, detailsMode).apply()
    }

    override fun setNightMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_NIGHT_MODE, enabled).apply()
    }

    override fun setMediaPlayerOption(option: Int) {
        prefs.edit().putInt(KEY_MEDIA_PLAYER, option).apply()
    }

    override fun completeFirstLaunch() {
        prefs.edit().putBoolean(IS_FIRST_TIME_LAUNCH, false).apply()
    }

    companion object {
        private const val PREF_NAME = "status_app"
        private const val PREF_SORT_KEY = "sort_key"
        private const val KEY_ACTIVITY_MODE = "activityMode"
        private const val IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch"
        private const val KEY_CHANNEL_COLUMNS = "keyColumns"
        private const val KEY_COL_COUNT = "rw_col_count"
        private const val KEY_NIGHT_MODE = "NightMode"
        private const val KEY_MEDIA_PLAYER = "media_player_key"
        private const val TYPE_GRID = "Grid"
        private const val TYPE_LIST = "List"
        private const val DEFAULT_COLUMNS = 3
        private const val DEFAULT_MEDIA_PLAYER = 2
    }
}
