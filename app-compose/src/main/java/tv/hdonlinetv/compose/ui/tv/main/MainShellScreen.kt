package tv.hdonlinetv.compose.ui.tv.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.NavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.Text
import androidx.tv.material3.rememberDrawerState
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.category.CategoryScreen
import tv.hdonlinetv.compose.ui.tv.channel.AllChannelsScreen
import tv.hdonlinetv.compose.ui.tv.favorites.FavoritesScreen
import tv.hdonlinetv.compose.ui.tv.playlist.PlaylistTabScreen

/** Same 4 sections as mobile tabs — live in the TV NavigationDrawer. */
private data class ShellSection(
    val titleRes: Int,
    val iconRes: Int,
)

private val shellSections = listOf(
    ShellSection(R.string.tab_playlists, R.drawable.ic_playlist),
    ShellSection(R.string.tab_channel, R.drawable.ic_tv_icon_white),
    ShellSection(R.string.tab_category, R.drawable.ic_local),
    ShellSection(R.string.tab_favorites, R.drawable.ic_favorite_border),
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainShellScreen() {
    val navController = LocalTvNavController.current
    // Open so labels are visible while debugging on a phone; TV D-pad still collapses.
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Open)
    var selectedSection by remember { mutableIntStateOf(0) }
    val colors = MaterialTheme.colorScheme
    val iconTint = colors.onSurface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        NavigationDrawer(
            drawerState = drawerState,
            modifier = Modifier.fillMaxSize(),
            drawerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(colors.surface)
                        .padding(12.dp)
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.Start,
                ) {
                    shellSections.forEachIndexed { index, section ->
                        NavigationDrawerItem(
                            selected = selectedSection == index,
                            onClick = { selectedSection = index },
                            modifier = Modifier.onFocusChanged { focus ->
                                if (focus.isFocused) selectedSection = index
                            },
                            leadingContent = {
                                Icon(
                                    painter = painterResource(section.iconRes),
                                    contentDescription = stringResource(section.titleRes),
                                    tint = iconTint,
                                )
                            },
                        ) {
                            Text(text = stringResource(section.titleRes))
                        }
                    }

                    NavigationDrawerItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.Search.route) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_actions_search),
                                contentDescription = null,
                                tint = iconTint,
                            )
                        },
                    ) {
                        Text(
                            text = stringResource(R.string.search_hint),
                            maxLines = 1,
                        )
                    }
                    NavigationDrawerItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.Settings.route) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_actions_settings),
                                contentDescription = null,
                                tint = iconTint,
                            )
                        },
                    ) {
                        Text(text = stringResource(R.string.menu_settings))
                    }
                    NavigationDrawerItem(
                        selected = false,
                        onClick = { navController.navigate(Routes.PlaylistManage.route) },
                        leadingContent = {
                            Icon(
                                painter = painterResource(R.drawable.ic_add_black_24dp),
                                contentDescription = null,
                                tint = iconTint,
                            )
                        },
                    ) {
                        Text(text = stringResource(R.string.playlist_management))
                    }
                }
            },
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colors.background),
            ) {
                when (selectedSection) {
                    0 -> PlaylistTabScreen()
                    1 -> AllChannelsScreen()
                    2 -> CategoryScreen()
                    else -> FavoritesScreen()
                }
            }
        }
    }
}
