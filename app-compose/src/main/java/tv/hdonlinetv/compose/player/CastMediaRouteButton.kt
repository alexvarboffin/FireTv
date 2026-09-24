package tv.hdonlinetv.compose.player

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.google.android.gms.cast.framework.CastContext
import tv.hdonlinetv.compose.R

/**
 * Cinema pattern: Compose icon fakes a click on the activity-hosted [CustomMediaRouteButton]
 * (see [MediaRouteButtonManager]) so the Cast dialog opens without embedding MediaRouteButton
 * in the player chrome.
 */
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

    IconButton(
        onClick = { MediaRouteButtonManager.current?.performClick() },
        modifier = modifier,
    ) {
        Icon(
            imageVector = Icons.Filled.Cast,
            contentDescription = stringResource(R.string.cast_content_description),
            tint = colorResource(R.color.white),
        )
    }
}
