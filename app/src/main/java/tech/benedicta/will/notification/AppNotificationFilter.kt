package tech.benedicta.will.notification

data class AppNotificationFilter(
    val packageName: String,
    val appName: String,
    val isEnabled: Boolean
)
