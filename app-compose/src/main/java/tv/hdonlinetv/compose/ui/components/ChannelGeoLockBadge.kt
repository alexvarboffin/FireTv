package tv.hdonlinetv.compose.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi

fun ChannelUi.isGeoBlocked(): Boolean = name.contains("Geo-blocked", ignoreCase = true)

@Composable
fun ChannelGeoLockBadge(
    channel: ChannelUi,
    modifier: Modifier = Modifier,
) {
    if (!channel.isGeoBlocked()) return

    val context = LocalContext.current
    Icon(
        painter = painterResource(R.drawable.ic_geolock),
        contentDescription = stringResource(R.string.geo_blocked),
        tint = Color(0xFFFF4444),
        modifier = modifier
            .size(24.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                Toast.makeText(context, context.getString(R.string.geo_blocked), Toast.LENGTH_SHORT).show()
            }
            .padding(4.dp),
    )
}
