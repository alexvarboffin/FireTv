package tv.hdonlinetv.compose.phone

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import tv.hdonlinetv.compose.core.databridge.channel.LocalChannelRepository as ChannelRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.category.LocalCategoryRepository as CategoryRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.playlist.LocalPlaylistRepository as PlaylistRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository as SettingsRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.xtream.LocalXtreamRepository as XtreamRepositoryImpl
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.ui.mobile.channel.ChannelListScreen
import tv.hdonlinetv.compose.ui.mobile.details.DetailsScreen
import tv.hdonlinetv.compose.ui.mobile.info.InfoWebScreen
import tv.hdonlinetv.compose.ui.mobile.info.TutorialScreen
import tv.hdonlinetv.compose.ui.mobile.main.MainShellScreen
import tv.hdonlinetv.compose.ui.mobile.onboarding.OnboardingScreen
import tv.hdonlinetv.compose.ui.mobile.player.PlayerScreen
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistChannelsScreen
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistManageScreen
import tv.hdonlinetv.compose.ui.mobile.playlist.SerialDetailScreen
import tv.hdonlinetv.compose.ui.mobile.playlist.XtreamBrowserScreen
import tv.hdonlinetv.compose.ui.mobile.search.SearchScreen
import tv.hdonlinetv.compose.ui.mobile.settings.SettingsScreen
import tv.hdonlinetv.compose.ui.mobile.splash.SplashScreen

@Composable
fun PhoneNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val categoryRepository = CategoryRepositoryImpl(context.applicationContext)
    val channelRepository = ChannelRepositoryImpl(context.applicationContext)
    val playlistRepository = PlaylistRepositoryImpl(context.applicationContext)
    val settingsRepository = SettingsRepositoryImpl(context.applicationContext)
    val xtreamRepository = XtreamRepositoryImpl()
    val gridColumns = settingsRepository.getSettings().gridColumns

    CompositionLocalProvider(
        LocalPhoneNavController provides navController,
        LocalCategoryRepository provides categoryRepository,
        LocalChannelRepository provides channelRepository,
        LocalPlaylistRepository provides playlistRepository,
        LocalSettingsRepository provides settingsRepository,
        LocalXtreamRepository provides xtreamRepository,
        LocalGridColumns provides gridColumns,
    ) {
        NavHost(
            navController = navController,
            startDestination = Routes.Splash.route,
        ) {
            composable(Routes.Splash.route) { SplashScreen() }
            composable(Routes.Onboarding.route) { OnboardingScreen() }
            composable(Routes.Main.route) { MainShellScreen() }
            composable(
                route = Routes.Channels.route,
                arguments = listOf(
                    navArgument(Routes.Channels.ARG_CATEGORY_NAME) { type = NavType.StringType },
                ),
            ) { ChannelListScreen() }
            composable(
                route = Routes.Details.route,
                arguments = listOf(
                    navArgument(Routes.Details.ARG_CHANNEL_ID) { type = NavType.LongType },
                    navArgument(Routes.Details.ARG_SCOPE) {
                        type = NavType.StringType
                        defaultValue = PlayerBrowseScope.TYPE_INFER
                    },
                    navArgument(Routes.Details.ARG_SCOPE_KEY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { DetailsScreen() }
            composable(
                route = Routes.Player.route,
                arguments = listOf(
                    navArgument(Routes.Player.ARG_CHANNEL_ID) { type = NavType.LongType },
                    navArgument(Routes.Player.ARG_STREAM_URL) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Routes.Player.ARG_STREAM_TITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                    navArgument(Routes.Player.ARG_SCOPE) {
                        type = NavType.StringType
                        defaultValue = PlayerBrowseScope.TYPE_INFER
                    },
                    navArgument(Routes.Player.ARG_SCOPE_KEY) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { PlayerScreen() }
            composable(Routes.Search.route) { SearchScreen() }
            composable(Routes.Settings.route) { SettingsScreen() }
            composable(Routes.PlaylistManage.route) { PlaylistManageScreen() }
            composable(
                route = Routes.PlaylistChannels.route,
                arguments = listOf(
                    navArgument(Routes.PlaylistChannels.ARG_PLAYLIST_ID) { type = NavType.LongType },
                    navArgument(Routes.PlaylistChannels.ARG_PLAYLIST_TITLE) { type = NavType.StringType },
                ),
            ) { PlaylistChannelsScreen() }
            composable(
                route = Routes.XtreamBrowser.route,
                arguments = listOf(
                    navArgument(Routes.XtreamBrowser.ARG_PLAYLIST_ID) { type = NavType.LongType },
                ),
            ) { XtreamBrowserScreen() }
            composable(
                route = Routes.SerialDetail.route,
                arguments = listOf(
                    navArgument(Routes.SerialDetail.ARG_PLAYLIST_ID) { type = NavType.LongType },
                    navArgument(Routes.SerialDetail.ARG_SERIES_ID) { type = NavType.IntType },
                    navArgument(Routes.SerialDetail.ARG_SERIES_TITLE) { type = NavType.StringType },
                ),
            ) { SerialDetailScreen() }
            composable(Routes.Tutorial.route) { TutorialScreen() }
            composable(
                route = Routes.InfoWeb.route,
                arguments = listOf(
                    navArgument(Routes.InfoWeb.ARG_URL) { type = NavType.StringType },
                    navArgument(Routes.InfoWeb.ARG_TITLE) {
                        type = NavType.StringType
                        defaultValue = ""
                    },
                ),
            ) { InfoWebScreen() }
        }
    }
}
