package tv.hdonlinetv.compose.navigation

import androidx.navigation.NavHostController
import tv.hdonlinetv.compose.ads.InterstitialAds
import tv.hdonlinetv.compose.core.domain.model.AppSettingsUi
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType

fun navigateToChannel(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
    scope: PlayerBrowseScope = PlayerBrowseScope.InferPlaylist,
) {
    // Phone interstitial scaffold; no-op on TV / when unit id is "0".
    InterstitialAds.maybeShowFrom(navController.context)
    if (channel.id > 0) {
        if (settings.detailsMode) {
            navController.navigate(Routes.Details.build(channel.id, scope))
        } else {
            navController.navigate(Routes.Player.build(channel.id, scope))
        }
    } else {
        navigateToStreamChannel(
            navController,
            channel,
            settings,
            scope,
            offerInterstitial = false,
        )
    }
}

fun navigateToXtreamItem(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
    playlistId: Long,
    isSeriesTab: Boolean,
    streamType: XtreamStreamType = if (isSeriesTab) XtreamStreamType.SERIES else XtreamStreamType.LIVE,
) {
    if (isSeriesTab) {
        val seriesId = channel.desc?.toIntOrNull() ?: return
        navController.navigate(
            Routes.SerialDetail.build(playlistId, seriesId, channel.name),
        )
    } else {
        navigateToChannel(
            navController,
            channel,
            settings,
            scope = PlayerBrowseScope.Xtream(playlistId, streamType),
        )
    }
}

fun navigateToStreamChannel(
    navController: NavHostController,
    channel: ChannelUi,
    settings: AppSettingsUi,
    scope: PlayerBrowseScope = PlayerBrowseScope.None,
    offerInterstitial: Boolean = true,
) {
    val streamUrl = channel.link.orEmpty()
    if (streamUrl.isBlank()) return
    if (offerInterstitial) {
        InterstitialAds.maybeShowFrom(navController.context)
    }
    if (settings.detailsMode && channel.id > 0) {
        navController.navigate(Routes.Details.build(channel.id, scope))
    } else {
        navController.navigate(Routes.Player.buildStream(streamUrl, channel.name, scope))
    }
}
