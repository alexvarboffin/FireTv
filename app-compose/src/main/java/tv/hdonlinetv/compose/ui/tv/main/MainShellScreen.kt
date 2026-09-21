package tv.hdonlinetv.compose.ui.tv.main

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModel
import tv.hdonlinetv.compose.core.presentation.category.CategoryViewModelFactory
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModel
import tv.hdonlinetv.compose.core.presentation.channel.ChannelListViewModelFactory
import tv.hdonlinetv.compose.core.presentation.channel.FavoritesViewModel
import tv.hdonlinetv.compose.core.presentation.channel.FavoritesViewModelFactory
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModel
import tv.hdonlinetv.compose.core.presentation.playlist.PlaylistViewModelFactory
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.phone.LocalCategoryRepository
import tv.hdonlinetv.compose.phone.LocalChannelRepository
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.category.CategoryScreen
import tv.hdonlinetv.compose.ui.tv.channel.AllChannelsScreen
import tv.hdonlinetv.compose.ui.tv.components.TvFocusTabRow
import tv.hdonlinetv.compose.ui.tv.favorites.FavoritesScreen
import tv.hdonlinetv.compose.ui.tv.playlist.PlaylistTabScreen

private val tabTitleRes = listOf(
    R.string.tab_playlists,
    R.string.tab_channel,
    R.string.tab_category,
    R.string.tab_favorites,
)

private data class DrawerNavItem(
    val titleRes: Int,
    val iconRes: Int,
    val onClick: () -> Unit,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainShellScreen() {
    val navController = LocalTvNavController.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val iconTint = colors.onSurface

    val playlistRepository = LocalPlaylistRepository.current
    val channelRepository = LocalChannelRepository.current
    val categoryRepository = LocalCategoryRepository.current
    val settingsRepository = LocalSettingsRepository.current
    val playlistViewModel: PlaylistViewModel = viewModel(
        factory = PlaylistViewModelFactory(playlistRepository),
    )
    val channelViewModel: ChannelListViewModel = viewModel(
        factory = ChannelListViewModelFactory(channelRepository, settingsRepository, categoryName = null),
    )
    val categoryViewModel: CategoryViewModel = viewModel(
        factory = CategoryViewModelFactory(categoryRepository),
    )
    val favoritesViewModel: FavoritesViewModel = viewModel(
        factory = FavoritesViewModelFactory(channelRepository),
    )
    val playlistState by playlistViewModel.uiState.collectAsState()
    val channelState by channelViewModel.uiState.collectAsState()
    val categoryState by categoryViewModel.uiState.collectAsState()
    val favoritesState by favoritesViewModel.uiState.collectAsState()
    val tabBadges = mapOf(
        0 to playlistState.playlists.size.toString(),
        1 to channelState.channels.size.toString(),
        2 to categoryState.categories.size.toString(),
        3 to favoritesState.channels.size.toString(),
    )
    val tabTitles = tabTitleRes.map { stringResource(it) }

    val drawerItems = listOf(
        DrawerNavItem(R.string.search_hint, R.drawable.ic_actions_search) {
            navController.navigate(Routes.Search.route)
        },
        DrawerNavItem(R.string.menu_settings, R.drawable.ic_actions_settings) {
            navController.navigate(Routes.Settings.route)
        },
        DrawerNavItem(R.string.playlist_management, R.drawable.ic_add_black_24dp) {
            navController.navigate(Routes.PlaylistManage.route)
        },
    )

    val screenFallback = remember { FocusRequester() }
    val tabRowFallback = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            ,
    ) {
        NavigationDrawer(
            drawerState = drawerState,
            modifier = Modifier.fillMaxSize(),
            drawerContent = {
                // LazyColumn: D-pad down scrolls when there are many drawer items.
                LazyColumn(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(colors.surface)
                        .selectableGroup(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    items(drawerItems, key = { it.titleRes }) { item ->
                        NavigationDrawerItem(
                            selected = false,
                            onClick = item.onClick,
                            leadingContent = {
                                Icon(
                                    painter = painterResource(item.iconRes),
                                    contentDescription = null,
                                    tint = iconTint,
                                )
                            },
                        ) {
                            Text(text = stringResource(item.titleRes), maxLines = 1)
                        }
                    }
                }
            },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background)
                    .focusRestorer(screenFallback)
                    .focusGroup(),
            ) {
                TvFocusTabRow(
                    tabs = tabTitles,
                    selectedIndex = selectedTabIndex,
                    onSelectedIndexChange = { selectedTabIndex = it },
                    badges = tabBadges,
                    tabRowFallback = tabRowFallback,
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .focusRequester(screenFallback)
                        .focusGroup(),
                ) {
                    when (selectedTabIndex) {
                        0 -> PlaylistTabScreen()
                        1 -> AllChannelsScreen()
                        2 -> CategoryScreen()
                        else -> FavoritesScreen()
                    }
                }
            }
        }
    }
}
