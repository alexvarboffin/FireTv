package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties

/**
 * Left from an item of the grid's first column → [target] (drawer).
 * The column comes from the grid's own layout at focus-search time, so it always matches
 * [androidx.compose.foundation.lazy.grid.GridCells.Adaptive] without measuring the width.
 */
fun Modifier.leftFromFirstColumn(
    state: LazyGridState,
    index: Int,
    target: FocusRequester?,
): Modifier =
    if (target == null) {
        this
    } else {
        focusProperties {
            val column = state.layoutInfo.visibleItemsInfo.firstOrNull { it.index == index }?.column
            if (column == 0) left = target
        }
    }
