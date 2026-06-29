package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.core.domain.model.CategoryUi
import tv.hdonlinetv.compose.ui.components.RemoteImage
import tv.hdonlinetv.compose.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryCard(
    category: CategoryUi,
    index: Int,
    onClick: () -> Unit,
) {
    val colors = stringArrayResource(R.array.category_colors)
    val bgColor = Color(android.graphics.Color.parseColor(colors[index % colors.size]))

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        colors = CardDefaults.colors(containerColor = bgColor),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x33000000)),
            )
            RemoteImage(
                url = category.thumb,
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 24.dp)
                    .fillMaxWidth(0.4f)
                    .aspectRatio(1f),
                contentScale = ContentScale.Crop,
            )
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
