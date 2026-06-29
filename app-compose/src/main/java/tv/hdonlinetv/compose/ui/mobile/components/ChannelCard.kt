package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.ChannelUi
import tv.hdonlinetv.compose.ui.components.ChannelGeoLockBadge
import tv.hdonlinetv.compose.ui.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChannelCard(
    channel: ChannelUi,
    onClick: () -> Unit,
) {
    val margin = dimensionResource(R.dimen.channel_card_margin)
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(margin),
        shape = RoundedCornerShape(dimensionResource(R.dimen.channel_card_radius)),
        colors = CardDefaults.cardColors(containerColor = colorResource(R.color.cardBack)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box {
                RemoteImage(
                    url = channel.cover,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop,
                )
                ChannelGeoLockBadge(
                    channel = channel,
                    modifier = Modifier.align(Alignment.TopEnd),
                )
            }
            Text(
                text = channel.name,
                fontSize = 14.sp,
                color = colorResource(R.color.black),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            )
        }
    }
}
