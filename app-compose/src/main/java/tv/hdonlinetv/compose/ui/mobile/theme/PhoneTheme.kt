package tv.hdonlinetv.compose.ui.mobile.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFFF0044),
    onPrimary = Color.White,
    secondary = Color(0xFFD50000),
    background = Color(0xFFFFFFFF),
    onBackground = Color(0xFF000000),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000),
)

/** Matches `values-night/colors.xml` (`bgMain` #101D24, `cardBack` #2E373C). */
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFFF0044),
    onPrimary = Color.White,
    secondary = Color(0xFFFF0044),
    background = Color(0xFF101D24),
    onBackground = Color.White,
    surface = Color(0xFF101D24),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF2E373C),
    onSurfaceVariant = Color(0xFFB0BEC5),
)

@Composable
fun PhoneTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        content = content,
    )
}
