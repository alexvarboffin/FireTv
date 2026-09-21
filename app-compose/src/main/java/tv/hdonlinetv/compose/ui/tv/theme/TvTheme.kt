package tv.hdonlinetv.compose.ui.tv.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.lightColorScheme

/**
 * TV themes copied from CinemaKMP:
 * - `LightColorsTv` / palette from `ui/theme/AppTheme.kt`
 * - Active dark scheme from `presentation/theme/Theme.kt` (JetStream) + `values/colors.xml`
 * - Wrapper matches `AppTheme(isTv = true)`: surface fill + LocalContentColor
 */

@OptIn(ExperimentalTvMaterial3Api::class)
private val LightColorsTv = lightColorScheme(
    primary = Color(0xFF5E35B1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3D8FD),
    onPrimaryContainer = Color(0xFF1A0057),
    secondary = Color(0xFF2196F3),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1E9FF),
    onSecondaryContainer = Color(0xFF001E34),
    tertiary = Color(0xFFFF5722),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDBD0),
    onTertiaryContainer = Color(0xFF3F0300),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1C1B1F),
)

/** Cinema JetStream dark — `colors.xml` + hard surface from `Theme.kt`. */
@OptIn(ExperimentalTvMaterial3Api::class)
private val JetStreamDarkColorScheme = darkColorScheme(
    primary = Color(0xFFA8C8FF),
    onPrimary = Color(0xFF003062),
    primaryContainer = Color(0xFF00468A),
    onPrimaryContainer = Color(0xFFD6E3FF),
    secondary = Color(0xFFBDC7DC),
    onSecondary = Color(0xFF273141),
    secondaryContainer = Color(0xFF3E4758),
    onSecondaryContainer = Color(0xFFD9E3F8),
    tertiary = Color(0xFFDCBCE1),
    onTertiary = Color(0xFF3E2845),
    tertiaryContainer = Color(0xFF563E5C),
    onTertiaryContainer = Color(0xFFF9D8FE),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF1A1C1E),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF43474E),
    onSurfaceVariant = Color(0xFFC4C6CF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFB4AB),
    border = Color(0xFF8E9099),
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvTheme(
    isDarkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    // Same as Cinema JetStreamTheme: always the dark XML scheme when dark.
    val colorScheme = if (isDarkTheme) JetStreamDarkColorScheme else LightColorsTv

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = MaterialTheme.shapes,
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                ) {
                    content()
                }
            }
        },
    )
}
