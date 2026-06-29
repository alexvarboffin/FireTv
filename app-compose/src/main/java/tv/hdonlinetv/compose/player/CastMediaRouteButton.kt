package tv.hdonlinetv.compose.player

import android.content.Context
import android.view.ContextThemeWrapper
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.gms.cast.framework.CastButtonFactory
import com.google.android.gms.cast.framework.CastContext
import tv.hdonlinetv.compose.R

@Composable
fun CastMediaRouteButton(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var castReady by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            CastContext.getSharedInstance(context.applicationContext)
            castReady = true
        } catch (_: Exception) {
            castReady = false
        }
    }

    if (!castReady) return

    AndroidView(
        modifier = modifier.size(48.dp),
        factory = { ctx -> createMediaRouteButton(ctx) },
    )
}

private fun createMediaRouteButton(context: Context): MediaRouteButton {
    val themedContext = ContextThemeWrapper(context, R.style.CastMediaRouteButtonTheme)
    return MediaRouteButton(themedContext).apply {
        CastButtonFactory.setUpMediaRouteButton(context.applicationContext, this)
    }
}
