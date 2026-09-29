package tv.hdonlinetv.compose.tv

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.focus.FocusRequester

/**
 * FocusRequester of the side NavigationDrawer **group**.
 * Only the **left edge** of a content group (tab index 0, grid col 0) sets
 * `focusProperties { left = it }` so Left lands on the group; inner siblings keep Default.
 * The group restores the child that was focused when we left.
 */
val LocalTvDrawerFocusRequester = staticCompositionLocalOf<FocusRequester?> { null }
