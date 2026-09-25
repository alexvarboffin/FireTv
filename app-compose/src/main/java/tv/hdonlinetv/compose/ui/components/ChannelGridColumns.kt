package tv.hdonlinetv.compose.ui.components

import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * [GridCells.Adaptive] minSize so a typical 360–430dp phone portrait is 3 columns.
 * Landscape / tablet get more. Matches Adaptive: (inner + spacing) / (min + spacing).
 */
val PhoneChannelGridMinCell = 104.dp

/**
 * Typical 1080p TV is 960dp; grid pad 48+48. 176.dp → 4 columns
 * (or more on a wider panel).
 */
val TvChannelGridMinCell = 176.dp
val TvChannelGridHPadding = 96.dp
val TvChannelGridSpacing = 16.dp

/** Same count [GridCells.Adaptive] uses, for TV first-column → drawer focus. */
fun adaptiveColumnCount(
    availableWidth: Dp,
    minCell: Dp,
    horizontalPadding: Dp = 0.dp,
    spacing: Dp = 0.dp,
): Int {
    val inner = (availableWidth - horizontalPadding).coerceAtLeast(0.dp)
    val cell = minCell + spacing
    if (cell <= 0.dp) return 1
    return maxOf(1, ((inner + spacing).value / cell.value).toInt())
}
