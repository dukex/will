package tech.benedicta.will.launcher

import android.content.Context
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.benedicta.will.task.Task

/**
 * The Android halves of the shortcut and the on-device model, run on the JVM
 * through Robolectric so `./gradlew test` answers them.
 *
 * `ActiveTaskShortcutPublisher` swallows every failure by design, so asking it
 * not to crash proves nothing. These tests read the shortcut back out of the
 * system instead.
 *
 * The model side is the honest half of what a machine without AICore can
 * check: that Will degrades rather than breaks. Whether Gemini Nano answers
 * *well* is a question only a device with the model can settle, and
 * `DeviceOnlyFeaturesTest` asks it there.
 */
@RunWith(RobolectricTestRunner::class)
class DeviceFeaturesJvmTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun activeTask() = Task(
        id = 7L,
        title = "responder Maria",
        text = "",
        sourcePackage = "com.whatsapp",
        createdAt = 0L,
        startedAt = 1_000L
    )

    // --- the active task as a launcher shortcut ---

    @Test
    fun `publishing the active task puts a shortcut on the system`() {
        val publisher = ActiveTaskShortcutPublisher(context)

        publisher.publish(activeTask())

        val published = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertTrue(
            "expected the active task shortcut, got ${published.map { it.id }}",
            published.any { it.id == ActiveTaskShortcut.ID }
        )
    }

    @Test
    fun `the shortcut carries the task it is for`() {
        val publisher = ActiveTaskShortcutPublisher(context)

        publisher.publish(activeTask())

        val shortcut = ShortcutManagerCompat.getDynamicShortcuts(context)
            .single { it.id == ActiveTaskShortcut.ID }
        assertEquals("responder Maria", shortcut.longLabel)
        assertEquals(
            7L,
            shortcut.intent.getLongExtra(ActiveTaskShortcutPublisher.EXTRA_TASK_ID, -1L)
        )
    }

    @Test
    fun `finishing the task takes the shortcut away`() {
        val publisher = ActiveTaskShortcutPublisher(context)
        publisher.publish(activeTask())

        publisher.publish(null)

        assertTrue(
            ShortcutManagerCompat.getDynamicShortcuts(context)
                .none { it.id == ActiveTaskShortcut.ID }
        )
    }

    @Test
    fun `publishing the same task twice leaves one shortcut, not two`() {
        val publisher = ActiveTaskShortcutPublisher(context)

        publisher.publish(activeTask())
        publisher.publish(activeTask())

        assertEquals(
            1,
            ShortcutManagerCompat.getDynamicShortcuts(context)
                .count { it.id == ActiveTaskShortcut.ID }
        )
    }

    // --- the on-device model, on a machine that has no AICore ---

    @Test
    fun `asking the model whether it is here answers instead of throwing`() {
        val model = GeminiNanoAppSearch(enabled = { true })

        val availability = runBlocking { model.availability() }

        assertEquals(
            "with no AICore there is no model, and that is an answer",
            ModelAvailability.UNAVAILABLE,
            availability
        )
    }

    @Test
    fun `a search with no model behind it comes back empty rather than failing`() {
        val model = GeminiNanoAppSearch(enabled = { true })

        val found = runBlocking {
            model.rank("banco", listOf(LauncherApp("com.itau", "Itaú")))
        }

        assertTrue(found.isEmpty())
    }

    @Test
    fun `a search with the feature off never reaches the model`() {
        val search = GeminiNanoAppSearch(enabled = { false })

        val found = runBlocking {
            search.rank("banco", listOf(LauncherApp("com.itau", "Itaú")))
        }

        assertTrue(found.isEmpty())
    }

    @Test
    fun `asking the absent model to download leaves it absent instead of crashing`() {
        val model = GeminiNanoAppSearch(enabled = { true })

        runBlocking { model.download() }

        assertEquals(
            ModelAvailability.UNAVAILABLE,
            runBlocking { model.availability() }
        )
    }

    // --- the promise the whole feature rests on ---

    @Test
    fun `with the model absent the drawer search behaves exactly as it did before`() {
        val installed = listOf(
            LauncherApp("com.whatsapp", "WhatsApp"),
            LauncherApp("com.itau", "Itaú")
        )
        val search = SearchWithMeaning(AppMatcher(), GeminiNanoAppSearch(enabled = { true }))

        val byName = runBlocking { search.filter(installed, "itau") }
        val byMeaning = runBlocking { search.filter(installed, "banco") }

        assertEquals(listOf("Itaú"), byName.map { it.name })
        assertTrue("no model, so no meaning, and nothing invented", byMeaning.isEmpty())
    }
}
