package tech.benedicta.will.launcher

import android.content.Context
import android.os.Build
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import tech.benedicta.will.task.Task

/**
 * The two things that only a real device can answer, written down so checking
 * them is one command rather than a memory of someone having looked.
 *
 * Run with: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class DeviceOnlyFeaturesTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

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
    fun publishing_the_active_task_puts_a_shortcut_on_the_system() {
        val publisher = ActiveTaskShortcutPublisher(context)

        publisher.publish(activeTask())

        val published = ShortcutManagerCompat.getDynamicShortcuts(context)
        assertTrue(
            "expected the active task shortcut, got ${published.map { it.id }}",
            published.any { it.id == ActiveTaskShortcut.ID }
        )
        publisher.publish(null)
    }

    @Test
    fun the_shortcut_carries_the_task_it_is_for() {
        val publisher = ActiveTaskShortcutPublisher(context)

        publisher.publish(activeTask())

        val shortcut = ShortcutManagerCompat.getDynamicShortcuts(context)
            .single { it.id == ActiveTaskShortcut.ID }
        assertEquals("responder Maria", shortcut.longLabel)
        assertEquals(
            7L,
            shortcut.intent?.getLongExtra(ActiveTaskShortcutPublisher.EXTRA_TASK_ID, -1L)
        )
        publisher.publish(null)
    }

    @Test
    fun finishing_the_task_takes_the_shortcut_away() {
        val publisher = ActiveTaskShortcutPublisher(context)
        publisher.publish(activeTask())

        publisher.publish(null)

        assertTrue(
            ShortcutManagerCompat.getDynamicShortcuts(context)
                .none { it.id == ActiveTaskShortcut.ID }
        )
    }

    // --- the on-device model ---

    @Test
    fun asking_the_model_whether_it_is_here_answers_without_throwing() {
        assumeTrue("needs API 26", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        val model = GeminiNanoAppSearch(enabled = { true })

        val availability = runBlocking { model.availability() }

        assertTrue(
            "every value is a legitimate answer; throwing is not",
            availability in ModelAvailability.entries
        )
    }

    @Test
    fun a_search_with_the_feature_off_never_reaches_the_model() {
        assumeTrue("needs API 26", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        val search = GeminiNanoAppSearch(enabled = { false })

        val found = runBlocking {
            search.rank("banco", listOf(LauncherApp("com.itau", "Itaú")))
        }

        assertTrue(found.isEmpty())
    }

    @Test
    fun a_search_on_a_phone_without_the_model_comes_back_empty_rather_than_failing() {
        assumeTrue("needs API 26", Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        val model = GeminiNanoAppSearch(enabled = { true })
        val availability = runBlocking { model.availability() }
        assumeTrue("this phone has the model, so there is nothing to assert here",
            availability != ModelAvailability.AVAILABLE)

        val found = runBlocking {
            model.rank("banco", listOf(LauncherApp("com.itau", "Itaú")))
        }

        assertTrue(found.isEmpty())
    }
}
