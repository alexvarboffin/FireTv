package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.core.domain.repository.XtreamStreamType
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.XtreamBrowserViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.navigation.navigateToXtreamItem
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.ChannelGridBody
import tv.hdonlinetv.compose.ui.tv.components.TvFocusTabRow
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay

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
    val selectedIndex = xtreamTabTypes.indexOf(state.selectedTab).coerceAtLeast(0)

    XtreamBrowserScreenBody(
        title = state.title.ifBlank { stringResource(R.string.xtream_browser) },
        tabTitles = stringArrayResource(R.array.tab_titles_xtream).toList(),
        selectedIndex = selectedIndex,
        channels = state.channels,
        onTabSelected = { index -> viewModel.selectTab(xtreamTabTypes[index]) },
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
    TvLoadingOverlay(visible = state.isLoading)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun XtreamBrowserScreenBody(
    title: String,
    tabTitles: List<String>,
    selectedIndex: Int,
    channels: List<ChannelUi>,
    onTabSelected: (Int) -> Unit,
    onChannelClick: (ChannelUi) -> Unit,
) {
    val tabFocus = remember { FocusRequester() }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 24.dp),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 48.dp),
        )
        TvFocusTabRow(
            tabs = tabTitles,
            selectedIndex = selectedIndex,
            onSelectedIndexChange = { index ->
                if (index != selectedIndex) onTabSelected(index)
            },
            tabRowFallback = tabFocus,
        )
        ChannelGridBody(
            channels = channels,
            isLoading = false,
            onChannelClick = onChannelClick,
        )
    }
    LaunchedEffect(Unit) {
        runCatching { tabFocus.requestFocus() }
    }
}
