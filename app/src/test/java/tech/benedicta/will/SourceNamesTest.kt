package tech.benedicta.will

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * No declaration is named with a character the filesystem might not take.
 *
 * Kotlin writes a class file per lambda, and the path it writes comes from the
 * enclosing function's name. On a machine whose locale is not UTF-8 the JVM
 * reports `sun.jnu.encoding` as ASCII, cannot encode that path, and the
 * compiler dies with `InvalidPathException: Malformed input` naming a file
 * nobody wrote by hand. Nothing points at the test that caused it.
 *
 * CI runs under a UTF-8 locale, so CI never sees this. A container with no
 * `LANG` set does, and then the project simply does not build. The cheapest
 * place to catch it is here, with a message that names the declaration.
 *
 * This is about declaration names only. Test *bodies* are free to use any
 * language: asserting on "Itaú" or "María" is the point of several suites and
 * never reaches a filename.
 */
class SourceNamesTest {

    @Test
    fun `no declaration is named with a non-ascii character`() {
        val offenders = sourceFiles().flatMap { file ->
            file.readLines().withIndex().mapNotNull { (index, line) ->
                DECLARATION.find(line)?.let { match ->
                    val name = match.groupValues[2]
                    if (name.any { it.code > LAST_ASCII }) {
                        "${file.path}:${index + 1}  ${name.trim()}"
                    } else {
                        null
                    }
                }
            }
        }.toList()

        assertEquals(
            "these names become file paths and will not compile on a machine " +
                "whose locale is not UTF-8; rename them using ASCII",
            emptyList<String>(),
            offenders
        )
    }

    @Test
    fun `the scan actually reaches the source`() {
        assertTrue("no Kotlin source found to scan", sourceFiles().count() > 10)
    }

    private fun sourceFiles(): Sequence<File> =
        sourceRoot().walkTopDown().filter { it.isFile && it.extension == "kt" }

    private fun sourceRoot(): File {
        val fromModule = File("src")
        return if (fromModule.isDirectory) fromModule else File("app/src")
    }

    private companion object {
        const val LAST_ASCII = 127

        val DECLARATION =
            Regex("""^\s*(fun|class|object|interface|enum class)\s+`([^`]+)`""")
    }
}
