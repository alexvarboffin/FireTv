package tv.hdonlinetv.compose.ui.tv.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Border
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import tv.hdonlinetv.compose.R
import tv.hdonlinetv.compose.core.domain.model.CategoryUi

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryCard(
    category: CategoryUi,
    index: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = stringArrayResource(R.array.category_colors)
    val bgColor = Color(android.graphics.Color.parseColor(colors[index % colors.size]))
    // Cinema GradientBg pairs two hues; ours pairs the tile color with the next palette color.
    val pairColor = Color(android.graphics.Color.parseColor(colors[(index + 1) % colors.size]))
    val shape = RoundedCornerShape(10.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val gradientAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.85f else 0.35f,
        label = "CategoryCard.gradientAlpha",
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.2f),
        shape = ClickableSurfaceDefaults.shape(shape),
        scale = ClickableSurfaceDefaults.scale(focusedScale = 1.05f),
        // Same focus ring as tv.material3 Card (CardDefaults.border); none when unfocused.
        border = ClickableSurfaceDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(width = 3.dp, color = MaterialTheme.colorScheme.border),
                shape = shape,
            ),
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = bgColor,
            focusedContainerColor = bgColor,
            pressedContainerColor = bgColor,
        ),
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(gradientAlpha)
                    .background(
                        Brush.radialGradient(
                            listOf(lerp(bgColor, Color.White, 0.35f), pairColor),
                        ),
                    ),
            )
            // Icon hidden: name centered like Cinema CategoriesScreenTv.
            // ic_launcher_round is an adaptive-icon XML on API 26+: painterResource can't load it,
            // Coil can (via the resource drawable).
            // val roundIcon = rememberAsyncImagePainter(R.mipmap.ic_launcher_round)
            // AsyncImage(
            //     model = category.thumb?.trim()?.takeIf { it.isNotBlank() }
            //         ?: R.mipmap.ic_launcher_round,
            //     contentDescription = null,
            //     placeholder = roundIcon,
            //     error = roundIcon,
            //     contentScale = ContentScale.Crop,
            //     modifier = Modifier
            //         .align(Alignment.Center)
            //         .padding(top = 24.dp)
            //         .fillMaxWidth(0.4f)
            //         .aspectRatio(1f)
            //         .clip(CircleShape),
            // )
            if (category.count > 0) {
                Text(
                    text = stringResource(R.string.channels_format, category.count),
                    color = Color.White,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Text(
                text = category.name,
                color = Color.White,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                style = TextStyle(shadow = Shadow(color = Color.Black, blurRadius = 8f)),
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )
        }
    }
}
