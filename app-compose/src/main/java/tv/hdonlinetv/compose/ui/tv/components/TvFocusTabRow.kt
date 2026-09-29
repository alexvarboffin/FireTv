package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Tab
import androidx.tv.material3.TabDefaults
import androidx.tv.material3.TabRow
import androidx.tv.material3.TabRowDefaults
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R

/**
 * TV segment row (Cinema [HomeTvScreen] TabRow pattern):
 * D-pad focus on a tab switches content — no OK/click required.
 *
 * Accent matches phone [LegacyTabRow]: `colorPrimary` (#FF0044) pill when focused,
 * `colorLight` pill + primary text when the row is not focused.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvFocusTabRow(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    badges: Map<Int, String> = emptyMap(),
    tabRowFallback: FocusRequester,
    leftFocusRequester: FocusRequester? = null,
    onTabFocusChanged: (Boolean) -> Unit = {},
) {
    val accent = colorResource(R.color.colorPrimary)
    val selectedPill = colorResource(R.color.colorLight)
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .focusRestorer(tabRowFallback)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        indicator = { tabPositions, doesTabRowHaveFocus ->
            tabPositions.getOrNull(selectedIndex)?.let { currentTabPosition ->
                TabRowDefaults.PillIndicator(
                    currentTabPosition = currentTabPosition,
                    doesTabRowHaveFocus = doesTabRowHaveFocus,
                    activeColor = accent,
                    inactiveColor = selectedPill,
                )
            }
        },
    ) {
        tabs.forEachIndexed { index, title ->
            val badge = badges[index]
            Tab(
                selected = selectedIndex == index,
                onFocus = { onSelectedIndexChange(index) },
                onClick = { onSelectedIndexChange(index) },
                colors = TabDefaults.pillIndicatorTabColors(
                    selectedContentColor = accent,
                    focusedContentColor = Color.White,
                    focusedSelectedContentColor = Color.White,
                ),
                modifier = Modifier
                    .then(if (index == 0) Modifier.focusRequester(tabRowFallback) else Modifier)
                    .then(
                        // Only the left edge of the TabRow group. Inner tabs keep Default
                        // so Left/Right stay in the row (tab-4 → tab-3, not the drawer).
                        if (index == 0 && leftFocusRequester != null) {
                            Modifier.focusProperties { left = leftFocusRequester }
                        } else {
                            Modifier
                        },
                    )
                    .onFocusChanged { onTabFocusChanged(it.isFocused) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title)
                    if (!badge.isNullOrBlank()) {
                        TabCountBadge(
                            text = badge,
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

/** Mirror of phone `LegacyTabRow` material3 `Badge` (tv.material3 has no Badge). */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TabCountBadge(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .defaultMinSize(minWidth = 20.dp, minHeight = 20.dp)
            .background(colorResource(R.color.badge_background_color), CircleShape)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = colorResource(R.color.badge_text_color),
            fontSize = 11.sp,
            maxLines = 1,
        )
    }
}
