package tech.benedicta.will

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The languages do not drift apart.
 *
 * A key present in `values-pt/` and missing from `values-es/` is not a build
 * failure: Android falls back to the default, so a Spanish phone shows English
 * in the middle of a Spanish screen and nothing says so. That is exactly how a
 * Spanish phone ended up reading "reply to Maria", and it is quiet enough to
 * survive a review, so it is checked here.
 *
 * This does not demand that every string be translated. `app_name` is the
 * product name and stays "Will" everywhere. What it demands is that whatever
 * is translated is translated into every language Will claims to speak.
 */
class TranslationsTest {

    @Test
    fun `every language carries the same keys as every other`() {
        val languages = translations()
        assertTrue("this test is meaningless without a translation", languages.isNotEmpty())

        val reference = languages.first()
        val expected = keysOf(reference)
        assertTrue("$reference should declare some strings", expected.isNotEmpty())

        languages.drop(1).forEach { language ->
            assertEquals(
                "$language does not carry the same keys as $reference",
                expected,
                keysOf(language)
            )
        }
    }

    @Test
    fun `a translated key is one the default declares`() {
        val default = keysOf("values")
        translations().forEach { language ->
            val unknown = keysOf(language) - default
            assertEquals("$language translates keys that values/ does not declare", emptySet<String>(), unknown)
        }
    }

    private fun translations(): List<String> =
        resourceRoot().listFiles()
            .orEmpty()
            .map { it.name }
            .filter { it.startsWith("values-") && File(resourceRoot(), "$it/strings.xml").isFile }
            .sorted()

    private fun keysOf(directory: String): Set<String> {
        val file = File(resourceRoot(), "$directory/strings.xml")
        assertTrue("${file.path} should exist", file.isFile)
        return KEY.findAll(file.readText()).map { it.groupValues[1] }.toSet()
    }

    private fun resourceRoot(): File {
        val fromModule = File("src/main/res")
        return if (fromModule.isDirectory) fromModule else File("app/src/main/res")
    }

    private companion object {
        val KEY = Regex("""<string\s+name="([^"]+)"""")
    }
}
