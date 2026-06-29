package tv.hdonlinetv.compose.tv

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalTvNavController = staticCompositionLocalOf<NavHostController> {
    error("TvNavController not provided")
}
