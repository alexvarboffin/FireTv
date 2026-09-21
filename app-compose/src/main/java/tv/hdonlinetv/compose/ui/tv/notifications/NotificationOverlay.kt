package tv.hdonlinetv.compose.ui.tv.notifications

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.Text
import kotlinx.coroutines.delay

/**
 * Non-focusable toast stack (LDS [NotificationOverlay] pattern for TV):
 * sits above content, never steals D-pad focus, auto-dismisses with animation.
 */
@Composable
fun NotificationOverlay(
    notifications: List<NotificationItem>,
    onDismiss: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (notifications.isEmpty()) return

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusProperties { canFocus = false },
        contentAlignment = Alignment.TopEnd,
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 420.dp)
                .focusProperties { canFocus = false },
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End,
            userScrollEnabled = false,
        ) {
            items(notifications, key = { it.id }) { item ->
                NotificationCard(
                    item = item,
                    onDismiss = { onDismiss(item.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun NotificationCard(
    item: NotificationItem,
    onDismiss: () -> Unit,
) {
    var visible by remember(item.id) { mutableStateOf(false) }
    val isSuccess = item.type == NotificationType.SUCCESS
    val backgroundColor = if (isSuccess) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val borderColor = if (isSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
    val accentColor = if (isSuccess) Color(0xFF2E7D32) else Color(0xFFC62828)

    LaunchedEffect(item.id) {
        visible = true
        delay(item.durationMillis)
        visible = false
        delay(280)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessLow,
            ),
        ) + fadeIn(),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        ) + fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .focusProperties { canFocus = false }
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = if (isSuccess) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = item.title,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = item.message,
                    color = Color.Black.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
    }
}
