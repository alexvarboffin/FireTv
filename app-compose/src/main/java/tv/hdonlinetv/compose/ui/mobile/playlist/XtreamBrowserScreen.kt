package tv.hdonlinetv.compose.ui.mobile.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToXtreamItem
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.ui.mobile.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTabRow
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

private val xtreamTabTypes = listOf(
    XtreamStreamType.LIVE,
    XtreamStreamType.VOD,
    XtreamStreamType.SERIES,
)

@Composable
fun XtreamBrowserScreen() {
    val navController = LocalPhoneNavController.current
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

@Composable
fun XtreamBrowserScreenBody(
    title: String,
    tabTitles: List<String>,
    pagerState: androidx.compose.foundation.pager.PagerState,
    channels: List<tv.hdonlinetv.compose.core.domain.model.ChannelUi>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onChannelClick: (tv.hdonlinetv.compose.core.domain.model.ChannelUi) -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null,
                            tint = colorResource(R.color.black),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(colorResource(R.color.bgMain)),
        ) {
            LegacyTabRow(
                tabs = tabTitles,
                selectedIndex = pagerState.currentPage,
                onTabSelected = onTabSelected,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) {
                ChannelGridBody(
                    channels = channels,
                    isLoading = isLoading,
                    onChannelClick = onChannelClick,
                )
            }
        }
    }
}
