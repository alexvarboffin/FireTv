package tv.hdonlinetv.compose.tv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.MaterialTheme
import tv.hdonlinetv.compose.core.databridge.channel.LocalChannelRepository as ChannelRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.category.LocalCategoryRepository as CategoryRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.playlist.LocalPlaylistRepository as PlaylistRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.settings.LocalSettingsRepository as SettingsRepositoryImpl
import tv.hdonlinetv.compose.core.databridge.xtream.LocalXtreamRepository as XtreamRepositoryImpl
import tv.hdonlinetv.compose.navigation.PlayerBrowseScope
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalCategoryRepository
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalGridColumns
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.ui.tv.channel.ChannelListScreen
import tv.hdonlinetv.compose.ui.tv.details.DetailsScreen
import tv.hdonlinetv.compose.ui.tv.info.InfoWebScreen
import tv.hdonlinetv.compose.ui.tv.info.TutorialScreen
import tv.hdonlinetv.compose.ui.tv.main.MainShellScreen
import tv.hdonlinetv.compose.ui.tv.notifications.LocalTvNotificationManager
import tv.hdonlinetv.compose.ui.tv.notifications.NotificationManager
import tv.hdonlinetv.compose.ui.tv.notifications.NotificationOverlay
import tv.hdonlinetv.compose.ui.tv.onboarding.OnboardingScreen
import tv.hdonlinetv.compose.ui.tv.player.PlayerScreen
import tv.hdonlinetv.compose.ui.tv.playlist.PlaylistChannelsScreen
import tv.hdonlinetv.compose.ui.tv.playlist.PlaylistManageScreen
import tv.hdonlinetv.compose.ui.tv.playlist.SerialDetailScreen
import tv.hdonlinetv.compose.ui.tv.playlist.XtreamBrowserScreen
import tv.hdonlinetv.compose.ui.tv.search.SearchScreen
import tv.hdonlinetv.compose.ui.tv.settings.SettingsScreen
import tv.hdonlinetv.compose.ui.tv.splash.SplashScreen

@Composable
fun TvNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val notificationManager = remember { NotificationManager() }
    val categoryRepository = CategoryRepositoryImpl(context.applicationContext)
    val channelRepository = ChannelRepositoryImpl(context.applicationContext)
    val playlistRepository = PlaylistRepositoryImpl(context.applicationContext)
    val settingsRepository = SettingsRepositoryImpl(context.applicationContext)
    val xtreamRepository = XtreamRepositoryImpl()
    val storedColumns = settingsRepository.getSettings().gridColumns
    // TV: grid mode is always 4 columns (phone settings may still store 3).
    val gridColumns = if (storedColumns <= 1) 1 else 4
    val colors = MaterialTheme.colorScheme
    CompositionLocalProvider(
        LocalTvNavController provides navController,
        LocalTvNotificationManager provides notificationManager,
        LocalCategoryRepository provides categoryRepository,
        LocalChannelRepository provides channelRepository,
        LocalPlaylistRepository provides playlistRepository,
        LocalSettingsRepository provides settingsRepository,
        LocalXtreamRepository provides xtreamRepository,
        LocalGridColumns provides gridColumns,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .background(colors.background)
                        .windowInsetsPadding(WindowInsets.systemBars),
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
                                navArgument(Routes.Channels.ARG_CATEGORY_NAME) {
                                    type = NavType.StringType
                                },
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
                        composable(
                            route = Routes.PlaylistChannels.route,
                            arguments = listOf(
                                navArgument(Routes.PlaylistChannels.ARG_PLAYLIST_ID) {
                                    type = NavType.LongType
                                },
                                navArgument(Routes.PlaylistChannels.ARG_PLAYLIST_TITLE) {
                                    type = NavType.StringType
                                },
                            ),
                        ) { PlaylistChannelsScreen() }
                        composable(
                            route = Routes.XtreamBrowser.route,
                            arguments = listOf(
                                navArgument(Routes.XtreamBrowser.ARG_PLAYLIST_ID) {
                                    type = NavType.LongType
                                },
                            ),
                        ) { XtreamBrowserScreen() }
                        composable(
                            route = Routes.SerialDetail.route,
                            arguments = listOf(
                                navArgument(Routes.SerialDetail.ARG_PLAYLIST_ID) {
                                    type = NavType.LongType
                                },
                                navArgument(Routes.SerialDetail.ARG_SERIES_ID) {
                                    type = NavType.IntType
                                },
                                navArgument(Routes.SerialDetail.ARG_SERIES_TITLE) {
                                    type = NavType.StringType
                                },
                            ),
                        ) { SerialDetailScreen() }
                    }
                }
            }
            NotificationOverlay(
                notifications = notificationManager.notifications,
                onDismiss = notificationManager::remove,
            )
        }
    }
}
