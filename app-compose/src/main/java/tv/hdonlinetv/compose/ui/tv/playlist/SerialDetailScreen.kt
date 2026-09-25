package tv.hdonlinetv.compose.ui.tv.playlist

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.SeriesEpisodeUi
import tv.hdonlinetv.compose.core.domain.model.SeriesInfoUi
import tv.hdonlinetv.compose.core.domain.model.SeriesSeasonUi
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailUiState
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.SerialDetailViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalXtreamRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.components.TvFocusTabRow
import tv.hdonlinetv.compose.ui.tv.components.TvLoadingOverlay

private const val TAB_INFO = 0
private const val TAB_SEASONS = 1
private const val TAB_EPISODES = 2

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
    val context = LocalContext.current

    SerialDetailScreenBody(
        state = state,
        tabTitles = stringArrayResource(R.array.tab_titles_xtream_serial).toList(),
        onSeasonClick = viewModel::selectSeason,
        onEpisodeClick = { episode ->
            val url = viewModel.buildEpisodeUrl(episode) ?: return@SerialDetailScreenBody
            val title = episode.title?.takeIf { it.isNotBlank() }
                ?: context.getString(R.string.episode_number_format, episode.episodeNum)
            navController.navigate(Routes.Player.buildStream(url, title))
        },
    )
    TvLoadingOverlay(visible = state.isLoading)
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SerialDetailScreenBody(
    state: SerialDetailUiState,
    tabTitles: List<String>,
    onSeasonClick: (Int) -> Unit,
    onEpisodeClick: (SeriesEpisodeUi) -> Unit,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(TAB_INFO) }
    var focusEpisodes by remember { mutableStateOf(false) }
    val tabFocus = remember { FocusRequester() }
    val firstEpisodeFocus = remember { FocusRequester() }
    val detail = state.detail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 24.dp),
    ) {
        Text(
            text = state.title.ifBlank { stringResource(R.string.tab_xtream_series) },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(horizontal = 48.dp),
        )
        if (detail == null) {
            if (!state.isLoading) EmptyText()
            return@Column
        }
        TvFocusTabRow(
            tabs = tabTitles,
            selectedIndex = selectedTab,
            onSelectedIndexChange = { selectedTab = it },
            tabRowFallback = tabFocus,
        )
        when (selectedTab) {
            TAB_INFO -> SeriesInfoTab(info = detail.info)
            TAB_SEASONS -> SeriesSeasonsTab(
                seasons = detail.seasons,
                onSeasonClick = { seasonNumber ->
                    onSeasonClick(seasonNumber)
                    selectedTab = TAB_EPISODES
                    focusEpisodes = true
                },
            )
            else -> SeriesEpisodesTab(
                episodes = detail.episodesBySeason[state.selectedSeason].orEmpty(),
                firstEpisodeFocus = firstEpisodeFocus,
                onEpisodeClick = onEpisodeClick,
            )
        }
    }

    LaunchedEffect(detail != null) {
        if (detail != null) runCatching { tabFocus.requestFocus() }
    }
    LaunchedEffect(focusEpisodes, selectedTab, state.selectedSeason) {
        if (focusEpisodes && selectedTab == TAB_EPISODES) {
            focusEpisodes = false
            val moved = runCatching { firstEpisodeFocus.requestFocus() }.isSuccess
            if (!moved) runCatching { tabFocus.requestFocus() }
        }
    }
}

@Composable
private fun EmptyText() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = stringResource(R.string.no_item))
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesInfoTab(info: SeriesInfoUi?) {
    val context = LocalContext.current
    if (info == null) {
        EmptyText()
        return
    }
    val rows = listOf(
        stringResource(R.string.name_label) to info.name,
        stringResource(R.string.cast_label) to info.cast,
        stringResource(R.string.category_id_label) to info.categoryId,
        stringResource(R.string.director_label) to info.director,
        stringResource(R.string.episode_run_time_label) to info.episodeRunTime,
        stringResource(R.string.genre_label) to info.genre,
        stringResource(R.string.last_modified_label) to info.lastModified,
        stringResource(R.string.plot_label) to info.plot,
        stringResource(R.string.rating_label) to info.rating,
        stringResource(R.string.rating_5based_label) to info.rating5Based,
        stringResource(R.string.release_date_label) to info.releaseDate,
    ).filter { !it.second.isNullOrBlank() }
    val trailerUrl = info.youtubeTrailer?.takeIf { it.isNotBlank() }?.let { trailer ->
        if (trailer.startsWith("http")) trailer else "https://www.youtube.com/watch?v=$trailer"
    }
    val trailerLabel = stringResource(R.string.youtube_trailer_label)

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(rows, key = { it.first }) { (label, value) ->
            // Each row is focusable so D-pad scrolls long plots block by block.
            TvInfoBlock(label = label, value = value.orEmpty(), onClick = {})
        }
        if (trailerUrl != null) {
            item(key = "trailer") {
                TvInfoBlock(
                    label = trailerLabel,
                    value = trailerUrl,
                    onClick = {
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, trailerUrl.toUri()))
                        }
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvInfoBlock(label: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Text(text = label, fontWeight = FontWeight.Bold)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesSeasonsTab(
    seasons: List<SeriesSeasonUi>,
    onSeasonClick: (Int) -> Unit,
) {
    if (seasons.isEmpty()) {
        EmptyText()
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(seasons, key = { it.id }) { season ->
            Surface(
                onClick = { onSeasonClick(season.seasonNumber) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = season.name
                            ?: stringResource(R.string.season_number_format, season.seasonNumber),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = stringResource(R.string.episode_count_format, season.episodeCount),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    season.overview?.takeIf { it.isNotBlank() }?.let { overview ->
                        Text(
                            text = overview,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun SeriesEpisodesTab(
    episodes: List<SeriesEpisodeUi>,
    firstEpisodeFocus: FocusRequester,
    onEpisodeClick: (SeriesEpisodeUi) -> Unit,
) {
    if (episodes.isEmpty()) {
        EmptyText()
        return
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 48.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(episodes, key = { _, episode -> episode.id }) { index, episode ->
            Surface(
                onClick = { onEpisodeClick(episode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (index == 0) Modifier.focusRequester(firstEpisodeFocus) else Modifier),
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = episode.title?.takeIf { it.isNotBlank() }
                            ?: stringResource(R.string.episode_number_format, episode.episodeNum),
                        fontWeight = FontWeight.Bold,
                    )
                    episode.plot?.takeIf { it.isNotBlank() }?.let { plot ->
                        Text(
                            text = plot,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
