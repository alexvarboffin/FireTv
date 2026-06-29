package tv.hdonlinetv.compose.ui.tv.playlist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.mobile.playlist.SerialDetailScreenBody

@Composable
fun SerialDetailScreen() {
    val navController = LocalTvNavController.current
    val args = navController.currentBackStackEntry?.arguments
    val playlistId = args?.getLong(Routes.SerialDetail.ARG_PLAYLIST_ID) ?: 0L
    val seriesId = args?.getInt(Routes.SerialDetail.ARG_SERIES_ID) ?: 0
    val seriesTitle = args?.getString(Routes.SerialDetail.ARG_SERIES_TITLE).orEmpty()
    val playlistRepository = LocalPlaylistRepository.current
    val xtreamRepository = LocalXtreamRepository.current
    val viewModel: SerialDetailViewModel = viewModel(
        factory = SerialDetailViewModelFactory(
            playlistRepository,
            xtreamRepository,
            playlistId,
            seriesId,
            seriesTitle,
        ),
    )
    val state by viewModel.uiState.collectAsState()
    val tabTitles = stringArrayResource(R.array.tab_titles_xtream_serial).toList()
    val pagerState = rememberPagerState(pageCount = { tabTitles.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    SerialDetailScreenBody(
        state = state,
        tabTitles = tabTitles,
        pagerState = pagerState,
        onBack = { navController.popBackStack() },
        onTabSelected = { index ->
            scope.launch { pagerState.animateScrollToPage(index) }
        },
        onSeasonClick = { seasonNumber ->
            viewModel.selectSeason(seasonNumber)
            scope.launch { pagerState.animateScrollToPage(2) }
        },
        onEpisodeClick = { episode ->
            val url = viewModel.buildEpisodeUrl(episode) ?: return@SerialDetailScreenBody
            val title = episode.title?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.episode_number_format, episode.episodeNum)
            navController.navigate(Routes.Player.buildStream(url, title))
        },
    )
}
