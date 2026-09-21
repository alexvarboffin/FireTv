package tv.hdonlinetv.compose.tv

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.focus.FocusRequester

/**
 * FocusRequester of the side NavigationDrawer focus-group ([Modifier.focusRestorer]).
 * Content leftmost items / first Tab should set `focusProperties { left = it }`
 * so D-pad Left returns to the drawer item the user left from.
 */
val LocalTvDrawerFocusRequester = staticCompositionLocalOf<FocusRequester?> { null }
