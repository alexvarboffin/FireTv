package tv.hdonlinetv.compose.ui.tv.notifications

import java.util.UUID

enum class NotificationType {
    SUCCESS,
    ERROR,
}

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val durationMillis: Long = 4_000L,
)
