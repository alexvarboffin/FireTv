package tv.hdonlinetv.compose.tv

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.focus.FocusRequester

/**
 * FocusRequester of the side NavigationDrawer **group**.
 * Leftmost content must set `focusProperties { left = it }` so Left lands on the group;
 * the group restores the child that was focused when we left (`saveFocusedChild` / enter).
 */
val LocalTvDrawerFocusRequester = staticCompositionLocalOf<FocusRequester?> { null }
