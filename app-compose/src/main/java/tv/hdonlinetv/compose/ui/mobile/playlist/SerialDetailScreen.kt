package tv.hdonlinetv.compose.ui.mobile.playlist

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.SeriesEpisodeUi
import tv.hdonlinetv.compose.core.domain.model.SeriesInfoUi
import tv.hdonlinetv.compose.core.domain.model.SeriesSeasonUi
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailUiState
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.ui.mobile.components.LegacyEmptyState
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTabRow
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar

@Composable
fun SerialDetailScreen() {
    val navController = LocalPhoneNavController.current
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

@Composable
fun SerialDetailScreenBody(
    state: SerialDetailUiState,
    tabTitles: List<String>,
    pagerState: PagerState,
    onBack: () -> Unit,
    onTabSelected: (Int) -> Unit,
    onSeasonClick: (Int) -> Unit,
    onEpisodeClick: (SeriesEpisodeUi) -> Unit,
) {
    Scaffold(
        containerColor = colorResource(R.color.bgMain),
        topBar = {
            LegacyTopAppBar(
                title = state.title.ifBlank { stringResource(R.string.tab_xtream_series) },
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
            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator(color = colorResource(R.color.colorPrimary))
                }
                return@Column
            }
            val detail = state.detail
            if (detail == null) {
                LegacyEmptyState(modifier = Modifier.fillMaxSize())
                return@Column
            }
            LegacyTabRow(
                tabs = tabTitles,
                selectedIndex = pagerState.currentPage,
                onTabSelected = onTabSelected,
            )
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                when (page) {
                    0 -> SeriesInfoTab(info = detail.info)
                    1 -> SeriesSeasonsTab(
                        seasons = detail.seasons,
                        onSeasonClick = onSeasonClick,
                    )
                    else -> SeriesEpisodesTab(
                        episodes = detail.episodesBySeason[state.selectedSeason].orEmpty(),
                        onEpisodeClick = onEpisodeClick,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesInfoTab(info: SeriesInfoUi?) {
    val context = LocalContext.current
    if (info == null) {
        LegacyEmptyState(modifier = Modifier.fillMaxSize())
        return
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        InfoRow(stringResource(R.string.name_label), info.name)
        InfoRow(stringResource(R.string.cast_label), info.cast)
        InfoRow(stringResource(R.string.category_id_label), info.categoryId)
        InfoRow(stringResource(R.string.director_label), info.director)
        InfoRow(stringResource(R.string.episode_run_time_label), info.episodeRunTime)
        InfoRow(stringResource(R.string.genre_label), info.genre)
        InfoRow(stringResource(R.string.last_modified_label), info.lastModified)
        InfoRow(stringResource(R.string.plot_label), info.plot)
        InfoRow(stringResource(R.string.rating_label), info.rating)
        InfoRow(stringResource(R.string.rating_5based_label), info.rating5Based)
        InfoRow(stringResource(R.string.release_date_label), info.releaseDate)
        val trailer = info.youtubeTrailer
        if (!trailer.isNullOrBlank()) {
            val url = if (trailer.startsWith("http")) trailer else "https://www.youtube.com/watch?v=$trailer"
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.youtube_trailer_label),
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.MainText),
                )
                Text(
                    text = url,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .clickable {
                            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                        },
                    color = colorResource(R.color.link_color),
                    textDecoration = TextDecoration.Underline,
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.MainText),
        )
        Text(
            text = value,
            modifier = Modifier.padding(start = 8.dp),
            color = colorResource(R.color.MainSecText),
        )
    }
}

@Composable
private fun SeriesSeasonsTab(
    seasons: List<SeriesSeasonUi>,
    onSeasonClick: (Int) -> Unit,
) {
    if (seasons.isEmpty()) {
        LegacyEmptyState(modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(seasons, key = { it.id }) { season ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSeasonClick(season.seasonNumber) }
                    .background(colorResource(R.color.cardBack))
                    .padding(12.dp),
            ) {
                Text(
                    text = season.name ?: stringResource(R.string.season_number_format, season.seasonNumber),
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.MainText),
                )
                Text(
                    text = stringResource(R.string.episode_count_format, season.episodeCount),
                    color = colorResource(R.color.MainSecText),
                    fontSize = 14.sp,
                )
                season.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                    Text(
                        text = overview,
                        modifier = Modifier.padding(top = 4.dp),
                        color = colorResource(R.color.MainSecText),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SeriesEpisodesTab(
    episodes: List<SeriesEpisodeUi>,
    onEpisodeClick: (SeriesEpisodeUi) -> Unit,
) {
    if (episodes.isEmpty()) {
        LegacyEmptyState(modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(episodes, key = { it.id }) { episode ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEpisodeClick(episode) }
                    .background(colorResource(R.color.cardBack))
                    .padding(12.dp),
            ) {
                Text(
                    text = episode.title?.takeIf { it.isNotBlank() }
                        ?: stringResource(R.string.episode_number_format, episode.episodeNum),
                    fontWeight = FontWeight.Bold,
                    color = colorResource(R.color.MainText),
                )
                episode.plot?.takeIf { it.isNotBlank() }?.let { plot ->
                    Text(
                        text = plot,
                        modifier = Modifier.padding(top = 4.dp),
                        color = colorResource(R.color.MainSecText),
                        fontSize = 13.sp,
                    )
                }
            }
        }
    }
}
