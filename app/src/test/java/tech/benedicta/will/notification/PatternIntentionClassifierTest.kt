package tech.benedicta.will.notification

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternIntentionClassifierTest {

    private val classifier = PatternIntentionClassifier()

    private suspend fun judge(title: String, text: String = "", pkg: String = "com.whatsapp") =
        classifier.isIntention(title = title, text = text, sourcePackage = pkg)

    // --- what a person actually has to act on ---

    @Test
    fun `a message from a person is an intention`() = runTest {
        assertTrue(judge("Maria", "can you send the contract?"))
    }

    @Test
    fun `a message in Portuguese is an intention`() = runTest {
        assertTrue(judge("Quezia", "passa no mercado na volta?"))
    }

    @Test
    fun `a calendar invite is an intention`() = runTest {
        assertTrue(judge("Reunião de arquitetura", "10:00 - 11:00", pkg = "com.google.android.calendar"))
    }

    @Test
    fun `a bank alert is an intention`() = runTest {
        assertTrue(judge("Boleto vence hoje", "R$ 212,40", pkg = "com.itau"))
    }

    // --- English noise, the cases the old regex list already covered ---

    @Test
    fun `download progress is noise`() = runTest {
        listOf(
            "Downloading video",
            "Downloading 2 videos",
            "Downloading file",
            "Downloading image"
        ).forEach { assertFalse(it, judge(it)) }
    }

    @Test
    fun `device housekeeping is noise`() = runTest {
        listOf(
            "Charging complete",
            "Update complete",
            "Could not connect to server",
            "PARAQUIACON has no internet access",
            "WIFI-GUEST has limited connectivity",
            "Upcoming alarm",
            "Your bedtime starts at 23:00",
            "Alarm volume is low"
        ).forEach { assertFalse(it, judge(it)) }
    }

    @Test
    fun `a bare app name is a group summary, not an intention`() = runTest {
        assertFalse(judge("WhatsApp"))
    }

    // --- Portuguese noise, which the English-only list let through ---

    @Test
    fun `download progress in Portuguese is noise`() = runTest {
        listOf(
            "Baixando vídeo",
            "Baixando 2 vídeos",
            "Baixando arquivo",
            "Baixando imagem",
            "Baixando foto"
        ).forEach { assertFalse(it, judge(it)) }
    }

    @Test
    fun `device housekeeping in Portuguese is noise`() = runTest {
        listOf(
            "Carregamento concluído",
            "Bateria carregada",
            "Atualização concluída",
            "Não foi possível conectar ao servidor",
            "PARAQUIACON está sem acesso à internet",
            "WIFI-GUEST tem conectividade limitada",
            "Próximo alarme",
            "Sua hora de dormir começa às 23:00",
            "Volume do alarme está baixo"
        ).forEach { assertFalse(it, judge(it)) }
    }

    @Test
    fun `accents are not required to match Portuguese noise`() = runTest {
        assertFalse(judge("Baixando video"))
        assertFalse(judge("Atualizacao concluida"))
    }

    // --- packages that never carry an intention ---

    @Test
    fun `a notification from a package on the ignore list is never an intention`() = runTest {
        NotificationProcessor.IGNORED_PACKAGES.forEach { pkg ->
            assertFalse(pkg, judge("Anything at all", pkg = pkg))
        }
    }

    // --- edge cases: when in doubt, keep it ---

    @Test
    fun `a title that merely mentions a noise word is still an intention`() = runTest {
        assertTrue(judge("Ana", "o download do contrato terminou, me avisa"))
        assertTrue(judge("Pedro", "a atualização do contrato está pronta"))
    }

    @Test
    fun `an unknown language is kept rather than guessed at`() = runTest {
        assertTrue(judge("Ana", "¿me mandas el contrato?"))
        assertTrue(judge("张伟", "合同发给我好吗"))
    }

    @Test
    fun `an empty title is not something to act on`() = runTest {
        assertFalse(judge("", ""))
        assertFalse(judge("   ", "   "))
    }

    @Test
    fun `matching is case-insensitive`() = runTest {
        assertFalse(judge("DOWNLOADING VIDEO"))
        assertFalse(judge("baixando VÍDEO"))
        assertFalse(judge("charging COMPLETE"))
    }
}
