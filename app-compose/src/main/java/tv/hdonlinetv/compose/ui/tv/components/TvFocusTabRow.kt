package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Tab
import androidx.tv.material3.TabRow
import androidx.tv.material3.Text

/**
 * TV segment row (Cinema [HomeTvScreen] TabRow pattern):
 * D-pad focus on a tab switches content — no OK/click required.
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
    val colors = MaterialTheme.colorScheme
    TabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier
            .focusRestorer(tabRowFallback)
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
    ) {
        tabs.forEachIndexed { index, title ->
            val badge = badges[index]
            Tab(
                selected = selectedIndex == index,
                onFocus = { onSelectedIndexChange(index) },
                onClick = { onSelectedIndexChange(index) },
                modifier = (if (index == 0) {
                    Modifier
                        .focusRequester(tabRowFallback)
                        .focusProperties {
                            leftFocusRequester?.let { left = it }
                        }
                } else {
                    Modifier
                }).onFocusChanged { onTabFocusChanged(it.isFocused) },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title)
                    if (!badge.isNullOrBlank()) {
                        Text(
                            text = " $badge",
                            color = colors.primary,
                            modifier = Modifier.padding(start = 4.dp),
                        )
                    }
                }
            }
        }
    }
}
