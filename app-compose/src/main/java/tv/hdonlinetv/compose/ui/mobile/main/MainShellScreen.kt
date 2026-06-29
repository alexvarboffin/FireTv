package tv.hdonlinetv.compose.ui.mobile.main

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
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
import tv.hdonlinetv.compose.phone.LocalPhoneNavController
import tv.hdonlinetv.compose.phone.LocalPlaylistRepository
import tv.hdonlinetv.compose.phone.LocalSettingsRepository
import tv.hdonlinetv.compose.ui.mobile.category.CategoryScreen
import tv.hdonlinetv.compose.ui.mobile.channel.AllChannelsScreen
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTabRow
import tv.hdonlinetv.compose.ui.mobile.components.LegacyTopAppBar
import tv.hdonlinetv.compose.ui.mobile.favorites.FavoritesScreen
import tv.hdonlinetv.compose.ui.mobile.playlist.PlaylistTabScreen
import androidx.core.net.toUri

private val tabTitleRes = listOf(
    R.string.tab_playlists,
    R.string.tab_channel,
    R.string.tab_category,
    R.string.tab_favorites,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainShellScreen() {
    val navController = LocalPhoneNavController.current
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabTitleRes.size })
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
    var showAboutDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text(stringResource(R.string.menu_about)) },
            text = {
                Text(
                    text = "${stringResource(R.string.app_name_legacy)}\n${stringResource(R.string.version_label)}: ${BuildConfig.VERSION_NAME}",
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text(stringResource(R.string.ok))
                }
            },
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = colorResource(R.color.bgMain),
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colorResource(R.color.bgMain))
                            .padding(horizontal = 16.dp, vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Image(
                            painter = painterResource(R.mipmap.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .padding(top = 30.dp),
                        )
                        Text(
                            text = stringResource(R.string.app_name_legacy),
                            color = colorResource(R.color.black),
                            fontSize = 16.sp,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    ) {
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_tv_icon,
                            label = stringResource(R.string.menu_home),
                            onClick = { scope.launch { drawerState.close() } },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_favorite_border,
                            label = stringResource(R.string.menu_profile),
                            onClick = {
                                scope.launch {
                                    drawerState.close()
                                    pagerState.animateScrollToPage(3)
                                }
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_actions_settings,
                            label = stringResource(R.string.menu_settings),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(Routes.Settings.route)
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_info,
                            label = stringResource(R.string.menu_about),
                            onClick = {
                                scope.launch { drawerState.close() }
                                showAboutDialog = true
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_ic_actions_star,
                            label = stringResource(R.string.menu_rate),
                            onClick = {
                                scope.launch { drawerState.close() }
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW,
                                        "market://details?id=${context.packageName}".toUri()),
                                )
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_share,
                            label = stringResource(R.string.menu_share),
                            onClick = {
                                scope.launch { drawerState.close() }
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, context.getString(R.string.app_name_legacy))
                                }
                                context.startActivity(Intent.createChooser(shareIntent, null))
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_ic_contact_mail,
                            label = stringResource(R.string.menu_feedback),
                            onClick = {
                                scope.launch { drawerState.close() }
                                val mailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("mailto:")
                                    putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.menu_feedback))
                                }
                                context.startActivity(mailIntent)
                            },
                        )
                        LegacyDrawerItem(
                            iconRes = R.drawable.ic_privacy,
                            label = stringResource(R.string.menu_privacy),
                            onClick = {
                                scope.launch { drawerState.close() }
                                navController.navigate(
                                    Routes.InfoWeb.build(
                                        url = context.getString(R.string.privacy_url),
                                        title = context.getString(R.string.policy_privacy),
                                    ),
                                )
                            },
                        )
                    }
                }
            }
        },
    ) {
        Scaffold(
            containerColor = colorResource(R.color.bgMain),
            topBar = {
                LegacyTopAppBar(
                    title = stringResource(tabTitleRes[pagerState.currentPage]),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_action_action),
                                contentDescription = null,
                                tint = colorResource(R.color.black),
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Routes.Search.route) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_actions_search),
                                contentDescription = null,
                                tint = colorResource(R.color.black),
                            )
                        }
                        IconButton(onClick = { navController.navigate(Routes.Tutorial.route) }) {
                            Icon(
                                painter = painterResource(R.drawable.ic_info),
                                contentDescription = null,
                                tint = colorResource(R.color.black),
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate(Routes.PlaylistManage.route) },
                    containerColor = colorResource(R.color.colorPrimary),
                    contentColor = colorResource(R.color.white),
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_add_black_24dp),
                        contentDescription = null,
                        tint = colorResource(R.color.black),
                    )
                }
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(colorResource(R.color.bgMain)),
            ) {
                LegacyTabRow(
                    tabs = tabTitleRes.map { stringResource(it) },
                    selectedIndex = pagerState.currentPage,
                    badges = tabBadges,
                    onTabSelected = { index ->
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize(),
                ) { page ->
                    when (page) {
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

@Composable
private fun LegacyDrawerItem(
    iconRes: Int,
    label: String,
    onClick: () -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(label, color = colorResource(R.color.tabText)) },
        selected = false,
        icon = {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = null,
                tint = colorResource(R.color.tabText),
            )
        },
        onClick = onClick,
    )
}
