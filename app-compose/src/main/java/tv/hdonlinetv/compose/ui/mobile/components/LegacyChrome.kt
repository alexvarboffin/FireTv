package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R

@Composable
fun LegacyTabRow(
    modifier: Modifier = Modifier,
    tabs: List<String>,
    selectedIndex: Int,
    badges: Map<Int, String> = emptyMap(),
    onTabSelected: (Int) -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colorResource(R.color.bgMain))
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        tabs.forEachIndexed { index, title ->
            val selected = index == selectedIndex
            val badge = badges[index]
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 5.dp)
                    .clip(RoundedCornerShape(300.dp))
                    .background(
                        if (selected) colorResource(R.color.colorLight) else Color.Transparent,
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        color = if (selected) {
                            colorResource(R.color.colorPrimary)
                        } else {
                            colorResource(R.color.black)
                        },
                    )
                    if (!badge.isNullOrBlank()) {
                        Badge(
                            modifier = Modifier.padding(start = 6.dp),
                            containerColor = colorResource(R.color.badge_background_color),
                            contentColor = colorResource(R.color.badge_text_color),
                        ) {
                            Text(text = badge, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material3 [TopAppBar] wrapper — status-bar / cutout insets come from
 * [TopAppBarDefaults.windowInsets] (works with Scaffold edge-to-edge).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyTopAppBar(
    modifier: Modifier = Modifier,
    title: String,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = colorResource(R.color.colorPrimaryDark),
    titleColor: Color = colorResource(R.color.black),
) {
    TopAppBar(
        modifier = modifier,
        title = {
            Text(
                text = title,
                color = titleColor,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        navigationIcon = navigationIcon,
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = backgroundColor,
            titleContentColor = titleColor,
            navigationIconContentColor = titleColor,
            actionIconContentColor = titleColor,
            scrolledContainerColor = backgroundColor,
        ),
        windowInsets = TopAppBarDefaults.windowInsets,
    )
}
