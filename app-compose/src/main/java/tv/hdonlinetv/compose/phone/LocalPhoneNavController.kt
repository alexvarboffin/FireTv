package tv.hdonlinetv.compose.phone

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController

val LocalPhoneNavController = staticCompositionLocalOf<NavHostController> {
    error("PhoneNavController not provided")
}
