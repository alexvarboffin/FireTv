package tv.hdonlinetv.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import tv.hdonlinetv.compose.R

/**
 * Remote cover/logo. [contentBackground] fills only the image bounds (same clip/shape
 * as [modifier]) so transparent PNGs sit on a placeholder plate, not the whole card.
 */
@Composable
fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderRes: Int = R.mipmap.ic_launcher_foreground,
    contentBackground: Color = colorResource(R.color.channel_icon_placeholder),
) {
    val placeholder = painterResource(placeholderRes)
    val model = url?.trim()?.takeIf { it.isNotBlank() } ?: placeholderRes
    Box(
        modifier = modifier.background(contentBackground),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = model,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = contentScale,
            placeholder = placeholder,
            error = placeholder,
        )
    }
}
