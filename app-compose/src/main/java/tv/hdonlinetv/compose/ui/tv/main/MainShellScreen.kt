package tv.hdonlinetv.compose.ui.tv.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.navigation.Routes
import tv.hdonlinetv.compose.tv.LocalTvNavController
import tv.hdonlinetv.compose.ui.tv.category.CategoryScreen
import tv.hdonlinetv.compose.ui.tv.channel.AllChannelsScreen
import tv.hdonlinetv.compose.ui.tv.favorites.FavoritesScreen
import tv.hdonlinetv.compose.ui.tv.playlist.PlaylistTabScreen

private val tabTitleRes = listOf(
    R.string.tab_playlists,
    R.string.tab_channel,
    R.string.tab_category,
    R.string.tab_favorites,
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainShellScreen() {
    val navController = LocalTvNavController.current
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { tabTitleRes.size })

    TabRow(
        modifier = Modifier.padding(horizontal = 48.dp, vertical = 24.dp),
        selectedTabIndex = pagerState.currentPage,
    ) {
        tabTitleRes.forEachIndexed { index, titleRes ->
            Tab(
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                onFocus = {},
            ) {
                Text(text = stringResource(titleRes))
            }
        }
    }
    HorizontalPager(state = pagerState) { page ->
        when (page) {
            0 -> PlaylistTabScreen()
            1 -> AllChannelsScreen()
            2 -> CategoryScreen()
            else -> FavoritesScreen()
        }
    }
}
