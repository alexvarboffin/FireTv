package tv.hdonlinetv.compose.navigation

import androidx.navigation.NavHostController
import tv.hdonlinetv.compose.core.domain.model.AppSettingsUi
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

fun navigateToChannel(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
) {
    if (channel.id > 0) {
        if (settings.detailsMode) {
            navController.navigate(Routes.Details.build(channel.id))
        } else {
            navController.navigate(Routes.Player.build(channel.id))
        }
    } else {
        navigateToStreamChannel(navController, channel, settings)
    }
}

fun navigateToXtreamItem(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
    playlistId: Long,
    isSeriesTab: Boolean,
) {
    if (isSeriesTab) {
        val seriesId = channel.desc?.toIntOrNull() ?: return
        navController.navigate(
            Routes.SerialDetail.build(playlistId, seriesId, channel.name),
        )
    } else {
        navigateToChannel(navController, channel, settings)
    }
}

fun navigateToStreamChannel(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
) {
    val streamUrl = channel.link.orEmpty()
    if (streamUrl.isBlank()) return
    if (settings.detailsMode && channel.id > 0) {
        navController.navigate(Routes.Details.build(channel.id))
    } else {
        navController.navigate(Routes.Player.buildStream(streamUrl, channel.name))
    }
}
