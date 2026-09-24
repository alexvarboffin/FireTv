package tv.hdonlinetv.compose.core.domain.repository

import tv.hdonlinetv.compose.core.domain.model.AppSettingsUi

interface SettingsRepository {
    fun getSettings(): AppSettingsUi
    fun setGridColumns(columns: Int)
    fun setSortOption(option: Int)
    fun setDetailsMode(detailsMode: Boolean)
    fun setNightMode(enabled: Boolean)
    fun setMediaPlayerOption(option: Int)
    fun setCleanupEmptyCategories(enabled: Boolean)
    fun completeFirstLaunch()
}
