package tv.hdonlinetv.compose.core.domain.model

data class AppSettingsUi(
    val gridColumns: Int = 3,
    val sortOption: Int = 0,
    val detailsMode: Boolean = false,
    val isFirstLaunch: Boolean = false,
    val nightMode: Boolean = false,
    val mediaPlayerOption: Int = 2,
)
