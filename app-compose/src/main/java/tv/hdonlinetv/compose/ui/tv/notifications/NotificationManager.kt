package tv.hdonlinetv.compose.ui.tv.notifications

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.staticCompositionLocalOf

class NotificationManager {
    var notifications = mutableStateListOf<NotificationItem>()
        private set

    fun show(title: String, message: String, type: NotificationType) {
        if (notifications.any { it.message == message && it.type == type }) return
        notifications.add(0, NotificationItem(title = title, message = message, type = type))
    }

    fun remove(id: String) {
        notifications.removeAll { it.id == id }
    }
}

val LocalTvNotificationManager = staticCompositionLocalOf<NotificationManager> {
    error("LocalTvNotificationManager not provided")
}
