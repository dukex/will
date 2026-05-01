package tech.benedicta.will.debug

import tech.benedicta.will.task.Task

/**
 * Curated seed for the Lucas persona — a developer / product designer mid-morning.
 * Used by the debug-only [DemoSeeder] to populate the home screen, drawer, and
 * focus dashboard with realistic data for screenshots and demos.
 */
data class DemoSeedData(
    val tasks: List<Task>,
    val pinnedPackages: List<String>
)

/**
 * Produces a deterministic seed anchored at [now] (epoch millis). All timestamps are
 * relative to [now] so the focus dashboard renders the seeded tasks as "today".
 */
fun buildLucasMorningSeed(now: Long): DemoSeedData {
    val minute = 60_000L
    val hour = 60 * minute

    val open = listOf(
        Task(
            title = "Sarah Chen",
            text = "did the canary deploy already pass? blocked on prod migration",
            sourcePackage = "com.slack",
            createdAt = now - 5 * minute,
            conversationKey = "com.slack:sarah chen",
            unreadCount = 3,
            notificationKey = "demo|com.slack|1|null|10001"
        ),
        Task(
            title = "github",
            text = "ana opened PR #482: refactor task lifecycle",
            sourcePackage = "com.github.android",
            createdAt = now - 12 * minute,
            conversationKey = "com.github.android:github",
            unreadCount = 1,
            notificationKey = "demo|com.github.android|2|null|10002"
        ),
        Task(
            title = "Marcus",
            text = "lunch?",
            sourcePackage = "com.whatsapp",
            createdAt = now - 28 * minute,
            conversationKey = "com.whatsapp:marcus",
            unreadCount = 2,
            notificationKey = "demo|com.whatsapp|3|null|10003"
        ),
        Task(
            title = "Calendar",
            text = "1:1 with Priya in 30 minutes",
            sourcePackage = "com.google.android.calendar",
            createdAt = now - 45 * minute,
            conversationKey = "com.google.android.calendar:calendar",
            unreadCount = 1,
            notificationKey = "demo|com.google.android.calendar|4|null|10004"
        ),
        Task(
            title = "Linear",
            text = "WIL-128 assigned to you: notification dedup edge case",
            sourcePackage = "app.linear",
            createdAt = now - hour - 10 * minute,
            conversationKey = "app.linear:linear",
            unreadCount = 1,
            notificationKey = "demo|app.linear|5|null|10005"
        )
    )

    val completed = listOf(
        Task(
            title = "Sarah Chen",
            text = "morning standup notes pushed",
            sourcePackage = "com.slack",
            createdAt = now - 2 * hour,
            startedAt = now - 2 * hour + minute,
            completedAt = now - hour - 45 * minute,
            contextSwitchCount = 2
        ),
        Task(
            title = "github",
            text = "PR #481 merged",
            sourcePackage = "com.github.android",
            createdAt = now - 3 * hour,
            startedAt = now - 3 * hour + 30_000L,
            completedAt = now - 2 * hour - 30 * minute,
            contextSwitchCount = 1
        ),
        Task(
            title = "Gmail",
            text = "weekly engineering digest",
            sourcePackage = "com.google.android.gm",
            createdAt = now - 4 * hour,
            startedAt = now - 4 * hour + 30_000L,
            completedAt = now - 3 * hour - 50 * minute,
            contextSwitchCount = 0
        ),
        Task(
            title = "Priya",
            text = "moved 1:1 to 11am",
            sourcePackage = "com.slack",
            createdAt = now - 5 * hour,
            startedAt = now - 5 * hour + 30_000L,
            completedAt = now - 5 * hour + 2 * minute,
            contextSwitchCount = 0
        ),
        Task(
            title = "github",
            text = "review requested on PR #479",
            sourcePackage = "com.github.android",
            createdAt = now - 6 * hour,
            startedAt = now - 6 * hour + 30_000L,
            completedAt = now - 5 * hour - 20 * minute,
            contextSwitchCount = 3
        )
    )

    val pinned = listOf(
        "com.android.chrome",
        "com.google.android.calendar",
        "com.google.android.gm",
        "com.slack",
        "com.github.android"
    )

    return DemoSeedData(
        tasks = open + completed,
        pinnedPackages = pinned
    )
}
