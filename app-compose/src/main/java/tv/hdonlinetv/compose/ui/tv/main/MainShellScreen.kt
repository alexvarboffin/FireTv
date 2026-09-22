package tv.hdonlinetv.compose.ui.tv.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.Button
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import tv.hdonlinetv.compose.BuildConfig
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
import tv.hdonlinetv.compose.tv.LocalTvDrawerFocusRequester
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
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAbout by remember { mutableStateOf(false) }
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

    // Mirror phone drawer + top-bar actions (Search / Tutorial) + FAB (Add playlist).
    val drawerItems = listOf(
        DrawerNavItem(R.string.menu_search, R.drawable.ic_actions_search) {
            navController.navigate(Routes.Search.route)
        },
        DrawerNavItem(R.string.menu_home, R.drawable.ic_tv_icon) {
            selectedTabIndex = 0
        },
        DrawerNavItem(R.string.menu_profile, R.drawable.ic_favorite_border) {
            selectedTabIndex = 3
        },
        DrawerNavItem(R.string.playlist_management, R.drawable.ic_add_black_24dp) {
            navController.navigate(Routes.PlaylistManage.route)
        },
        DrawerNavItem(R.string.menu_settings, R.drawable.ic_actions_settings) {
            navController.navigate(Routes.Settings.route)
        },
        DrawerNavItem(R.string.menu_tutorial, R.drawable.ic_info) {
            navController.navigate(Routes.Tutorial.route)
        },
        DrawerNavItem(R.string.menu_about, R.drawable.ic_info) {
            showAbout = true
        },
        DrawerNavItem(R.string.menu_rate, R.drawable.ic_ic_actions_star) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "market://details?id=${context.packageName}".toUri(),
                ),
            )
        },
        DrawerNavItem(R.string.menu_share, R.drawable.ic_share) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_name_legacy))
            }
            context.startActivity(Intent.createChooser(shareIntent, null))
        },
        DrawerNavItem(R.string.menu_feedback, R.drawable.ic_ic_contact_mail) {
            val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.menu_feedback))
            }
            context.startActivity(mailIntent)
        },
        DrawerNavItem(R.string.menu_privacy, R.drawable.ic_privacy) {
            navController.navigate(
                Routes.InfoWeb.build(
                    url = context.getString(R.string.privacy_url),
                    title = context.getString(R.string.policy_privacy),
                ),
            )
        },
    )

    val screenFallback = remember { FocusRequester() }
    val tabRowFallback = remember { FocusRequester() }
    val drawerGroupFocus = remember { FocusRequester() }
    val drawerFallback = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        CompositionLocalProvider(LocalTvDrawerFocusRequester provides drawerGroupFocus) {
            NavigationDrawer(
                drawerState = drawerState,
                modifier = Modifier.fillMaxSize(),
                drawerContent = {
                    // focusRestorer remembers the drawer item we left from;
                    // content leftmost Left → drawerGroupFocus restores that child.
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxHeight()
                            .background(colors.surface)
                            .focusRequester(drawerGroupFocus)
                            .focusRestorer(drawerFallback)
                            .focusGroup()
                            .selectableGroup(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        itemsIndexed(drawerItems, key = { _, item -> item.titleRes }) { index, item ->
                            NavigationDrawerItem(
                                selected = false,
                                onClick = item.onClick,
                                modifier = if (index == 0) {
                                    Modifier.focusRequester(drawerFallback)
                                } else {
                                    Modifier
                                },
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
                        leftFocusRequester = drawerGroupFocus,
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

        if (showAbout) {
            TvAboutDialog(onDismiss = { showAbout = false })
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvAboutDialog(onDismiss: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val okFocus = remember { FocusRequester() }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
        ),
    ) {
        LaunchedEffect(Unit) {
            okFocus.requestFocus()
        }
        Column(
            modifier = Modifier
                .width(480.dp)
                .background(colors.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = stringResource(R.string.menu_about))
            Text(
                text = "${stringResource(R.string.app_name_legacy)}\n" +
                    "${stringResource(R.string.version_label)}: ${BuildConfig.VERSION_NAME}",
            )
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .focusRequester(okFocus)
                    .onKeyEvent { event ->
                        if (event.type == KeyEventType.KeyDown &&
                            (event.key == Key.DirectionLeft || event.key == Key.DirectionRight)
                        ) {
                            onDismiss()
                            true
                        } else {
                            false
                        }
                    },
            ) {
                Text(text = stringResource(R.string.ok))
            }
        }
    }
}
