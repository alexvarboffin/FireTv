package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer

/**
 * Two Cinema cases:
 *
 * **Simple container** (Column/Box/Row): `createInitialFocusRestorerModifiers` —
 * `focusProperties` enter/exit + [FocusRequester.saveFocusedChild] /
 * [FocusRequester.restoreFocusedChild]. Children exist immediately.
 *
 * **LazyList / LazyGrid**: items compose on demand, so enter/exit restore misses the child.
 * Put [focusRequester] (the **group**) + [focusRestorer] (fallback child) **on the Lazy***
 * itself. First item: [focusRequester] of that fallback. Left from content → group FR;
 * the Lazy restorer brings back the last focused item.
 *
 * Cinema: [MovieCategoryRowTv], [MoviesRow], [SettingsSideMenu] LazyColumn,
 * [SearchScreenTv] grid — `focusRequester(group).focusRestorer(firstItem)`.
 */
data class TvLazyFocusRestorer(
    val group: FocusRequester,
    val child: FocusRequester,
) {
    fun childModifier(): Modifier = Modifier.focusRequester(child)
}

@Composable
fun rememberTvLazyFocusRestorer(): TvLazyFocusRestorer {
    val group = remember { FocusRequester() }
    val child = remember { FocusRequester() }
    return TvLazyFocusRestorer(group = group, child = child)
}

fun Modifier.tvLazyFocusGroup(restorer: TvLazyFocusRestorer): Modifier =
    focusRequester(restorer.group).focusRestorer(restorer.child)
