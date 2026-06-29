package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.pager.rememberPagerState
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToXtreamItem
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.playlist.XtreamBrowserScreenBody

private val xtreamTabTypes = listOf(
    XtreamStreamType.LIVE,
    XtreamStreamType.VOD,
    XtreamStreamType.SERIES,
)

@Composable
fun XtreamBrowserScreen() {
    val navController = LocalTvNavController.current
    val playlistId = navController.currentBackStackEntry
        ?.arguments
        ?.getLong(Routes.XtreamBrowser.ARG_PLAYLIST_ID) ?: 0L
    val playlistRepository = LocalPlaylistRepository.current
    val xtreamRepository = LocalXtreamRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val viewModel: XtreamBrowserViewModel = viewModel(
        factory = XtreamBrowserViewModelFactory(playlistRepository, xtreamRepository, playlistId),
    )
    val state by viewModel.uiState.collectAsState()
    val settings = settingsRepository.getSettings()
    val tabTitles = stringArrayResource(R.array.tab_titles_xtream).toList()
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()

    LaunchedEffect(pagerState.currentPage) {
        viewModel.selectTab(xtreamTabTypes[pagerState.currentPage])
    }

    XtreamBrowserScreenBody(
        title = state.title.ifBlank { stringResource(R.string.xtream_browser) },
        tabTitles = tabTitles,
        pagerState = pagerState,
        channels = state.channels,
        isLoading = state.isLoading,
        onBack = { navController.popBackStack() },
        onTabSelected = { index ->
            scope.launch { pagerState.animateScrollToPage(index) }
        },
        onChannelClick = { channel ->
            navigateToXtreamItem(
                navController,
                channel,
                settings,
                playlistId,
                isSeriesTab = state.selectedTab == XtreamStreamType.SERIES,
            )
        },
    )
}
