package tv.hdonlinetv.compose.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import tv.hdonlinetv.compose.R

@Composable
fun RemoteImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderRes: Int = R.mipmap.ic_launcher_foreground,
) {
    val placeholder = painterResource(placeholderRes)
    val model = url?.trim()?.takeIf { it.isNotBlank() } ?: placeholderRes
    AsyncImage(
        model = model,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        placeholder = placeholder,
        error = placeholder,
    )
}
