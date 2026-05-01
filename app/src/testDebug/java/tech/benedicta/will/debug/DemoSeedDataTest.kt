package tech.benedicta.will.debug

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DemoSeedDataTest {

    private val now = 1_730_000_000_000L

    @Test
    fun `seed has five open tasks and five completed tasks`() {
        val seed = buildLucasMorningSeed(now)

        val open = seed.tasks.filter { it.completedAt == null }
        val completed = seed.tasks.filter { it.completedAt != null }

        assertEquals(5, open.size)
        assertEquals(5, completed.size)
    }

    @Test
    fun `open tasks all have unread count of at least one`() {
        val seed = buildLucasMorningSeed(now)

        seed.tasks.filter { it.completedAt == null }.forEach { task ->
            assertTrue(
                "${task.title} should have unreadCount >= 1, was ${task.unreadCount}",
                task.unreadCount >= 1
            )
        }
    }

    @Test
    fun `at least one open task has unread count above one for the N-new badge`() {
        val seed = buildLucasMorningSeed(now)

        val withMultipleUnread = seed.tasks.filter {
            it.completedAt == null && it.unreadCount > 1
        }

        assertTrue(
            "expected at least one open task with unreadCount > 1, found none",
            withMultipleUnread.isNotEmpty()
        )
    }

    @Test
    fun `completed tasks have startedAt before completedAt`() {
        val seed = buildLucasMorningSeed(now)

        seed.tasks.filter { it.completedAt != null }.forEach { task ->
            assertNotNull("${task.title} missing startedAt", task.startedAt)
            assertTrue(
                "${task.title}: startedAt should be < completedAt",
                task.startedAt!! < task.completedAt!!
            )
        }
    }

    @Test
    fun `completed tasks fall within the last 24 hours so the dashboard shows them as today`() {
        val seed = buildLucasMorningSeed(now)
        val twentyFourHoursMs = 24L * 60 * 60 * 1000

        seed.tasks.filter { it.completedAt != null }.forEach { task ->
            val age = now - task.completedAt!!
            assertTrue(
                "${task.title} completedAt is older than 24h: age=$age",
                age in 0..twentyFourHoursMs
            )
        }
    }

    @Test
    fun `completed tasks release the conversation key so dedup is honest`() {
        val seed = buildLucasMorningSeed(now)

        seed.tasks.filter { it.completedAt != null }.forEach { task ->
            assertNull(
                "${task.title}: completed task should have null conversationKey",
                task.conversationKey
            )
        }
    }

    @Test
    fun `open tasks set the conversation key for dedup tests`() {
        val seed = buildLucasMorningSeed(now)

        seed.tasks.filter { it.completedAt == null }.forEach { task ->
            assertNotNull(
                "${task.title}: open task should have a conversationKey",
                task.conversationKey
            )
        }
    }

    @Test
    fun `total context switches is non-zero for a believable dashboard`() {
        val seed = buildLucasMorningSeed(now)

        val totalSwitches = seed.tasks.sumOf { it.contextSwitchCount }

        assertTrue(
            "expected at least one context switch across seeded tasks, got $totalSwitches",
            totalSwitches > 0
        )
    }

    @Test
    fun `seed pins exactly the launcher max of five apps`() {
        val seed = buildLucasMorningSeed(now)

        assertEquals(5, seed.pinnedPackages.size)
    }

    @Test
    fun `pinned packages have no duplicates`() {
        val seed = buildLucasMorningSeed(now)

        assertEquals(seed.pinnedPackages.toSet().size, seed.pinnedPackages.size)
    }

    @Test
    fun `seed spans multiple source packages so the feed looks varied`() {
        val seed = buildLucasMorningSeed(now)

        val sources = seed.tasks.map { it.sourcePackage }.toSet()

        assertTrue(
            "expected at least 4 distinct source packages, got ${sources.size}: $sources",
            sources.size >= 4
        )
    }

    @Test
    fun `seed is deterministic for a given now`() {
        val first = buildLucasMorningSeed(now)
        val second = buildLucasMorningSeed(now)

        assertEquals(first, second)
    }
}
