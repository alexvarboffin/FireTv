package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.focus.onFocusChanged

/**
 * Two Cinema cases:
 *
 * **Simple container** (Column/Box/Row): `createInitialFocusRestorerModifiers` —
 * `focusProperties` enter/exit + [FocusRequester.saveFocusedChild] /
 * [FocusRequester.restoreFocusedChild]. Children exist immediately.
 *
 * **LazyList / LazyGrid**: items compose on demand, so enter/exit restore misses the child.
 * Put [focusRequester] (the **group**) + [focusRestorer] (fallback child) **on the Lazy***
 * itself. Left from content → group FR; the Lazy restorer brings back the last focused item.
 *
 * Cinema: [MovieCategoryRowTv], [MoviesRow], [SettingsSideMenu] LazyColumn,
 * [SearchScreenTv] grid — `focusRequester(group).focusRestorer(firstItem)`;
 * [MoviesScreenTv] — fallback child follows `lastFocusedIndex`.
 *
 * [itemModifier] puts the fallback child on the **last focused** item (saved across
 * navigation), so returning from player / details lands on the item the user left.
 */
@Stable
class TvLazyFocusRestorer internal constructor(
    val group: FocusRequester,
    val child: FocusRequester,
    private val lastFocused: MutableIntState,
    internal var restorePending: Boolean,
) {
    val lastFocusedIndex: Int get() = lastFocused.intValue

    fun childModifier(): Modifier = Modifier.focusRequester(child)

    fun itemModifier(index: Int, itemCount: Int): Modifier {
        val target = lastFocused.intValue.coerceIn(0, (itemCount - 1).coerceAtLeast(0))
        return (if (index == target) childModifier() else Modifier)
            .onFocusChanged { if (it.isFocused) lastFocused.intValue = index }
    }
}

@Composable
fun rememberTvLazyFocusRestorer(): TvLazyFocusRestorer {
    val group = remember { FocusRequester() }
    val child = remember { FocusRequester() }
    val lastFocused = rememberSaveable { mutableIntStateOf(-1) }
    return remember {
        TvLazyFocusRestorer(
            group = group,
            child = child,
            lastFocused = lastFocused,
            restorePending = lastFocused.intValue >= 0,
        )
    }
}

fun Modifier.tvLazyFocusGroup(restorer: TvLazyFocusRestorer): Modifier =
    focusRequester(restorer.group).focusRestorer(restorer.child)

/** Back from another destination: scroll to the saved item and give it focus. */
@Composable
fun TvLazyFocusRestorer.RestoreFocusOnReturn(state: LazyListState, itemCount: Int) =
    RestoreFocusOnReturn(
        itemCount = itemCount,
        isVisible = { index -> state.layoutInfo.visibleItemsInfo.any { it.index == index } },
        scrollTo = { state.scrollToItem(it) },
    )

@Composable
fun TvLazyFocusRestorer.RestoreFocusOnReturn(state: LazyGridState, itemCount: Int) =
    RestoreFocusOnReturn(
        itemCount = itemCount,
        isVisible = { index -> state.layoutInfo.visibleItemsInfo.any { it.index == index } },
        scrollTo = { state.scrollToItem(it) },
    )

@Composable
private fun TvLazyFocusRestorer.RestoreFocusOnReturn(
    itemCount: Int,
    isVisible: (Int) -> Boolean,
    scrollTo: suspend (Int) -> Unit,
) {
    LaunchedEffect(itemCount > 0) {
        if (!restorePending || itemCount == 0) return@LaunchedEffect
        restorePending = false
        val target = lastFocusedIndex.coerceIn(0, itemCount - 1)
        withFrameNanos { }
        if (!isVisible(target)) scrollTo(target)
        withFrameNanos { }
        runCatching { child.requestFocus() }
    }
}
