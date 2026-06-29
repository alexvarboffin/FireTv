package tv.hdonlinetv.compose.ui.mobile.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.ui.components.RemoteImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    category: CategoryUi,
    index: Int,
    onClick: () -> Unit,
) {
    val colors = stringArrayResource(R.array.category_colors)
    val bgColor = Color(android.graphics.Color.parseColor(colors[index % colors.size]))
    val cardHeight = dimensionResource(R.dimen.category_card_height)
    val cardMargin = dimensionResource(R.dimen.category_card_margin)
    val iconSize = dimensionResource(R.dimen.category_icon_size)

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(cardMargin)
            .height(cardHeight),
        shape = RoundedCornerShape(dimensionResource(R.dimen.category_card_radius)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgColor),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorResource(R.color.categoryOverlay)),
            )
            if (category.thumb.isNullOrBlank()) {
                Icon(
                    painter = painterResource(R.drawable.ic_tv_icon_white),
                    contentDescription = null,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.Center),
                    tint = Color.White,
                )
            } else {
                RemoteImage(
                    url = category.thumb,
                    modifier = Modifier
                        .size(iconSize)
                        .align(Alignment.Center),
                    contentScale = ContentScale.Crop,
                )
            }
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(10.dp),
            )
        }
    }
}
