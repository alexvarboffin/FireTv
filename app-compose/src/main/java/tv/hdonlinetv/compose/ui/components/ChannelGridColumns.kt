package tv.hdonlinetv.compose.ui.components

import androidx.compose.foundation.lazy.grid.GridCells
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
