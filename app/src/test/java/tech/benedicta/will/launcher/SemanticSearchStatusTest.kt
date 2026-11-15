package tech.benedicta.will.launcher

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class SemanticSearchStatusTest {

    private fun state(enabled: Boolean, availability: ModelAvailability) =
        SemanticSearchStatus.of(enabled, availability)

    @Test
    fun `off and ready is simply off`() {
        assertEquals(
            SemanticSearchStatus.OFF,
            state(enabled = false, availability = ModelAvailability.AVAILABLE)
        )
    }

    @Test
    fun `on and ready is working`() {
        assertEquals(
            SemanticSearchStatus.ON,
            state(enabled = true, availability = ModelAvailability.AVAILABLE)
        )
    }

    @Test
    fun `on but the model is not here yet asks for the download`() {
        assertEquals(
            SemanticSearchStatus.NEEDS_DOWNLOAD,
            state(enabled = true, availability = ModelAvailability.DOWNLOADABLE)
        )
    }

    @Test
    fun `a download in progress says so rather than looking broken`() {
        assertEquals(
            SemanticSearchStatus.DOWNLOADING,
            state(enabled = true, availability = ModelAvailability.DOWNLOADING)
        )
    }

    @Test
    fun `a phone that cannot run the model says that, not off`() {
        assertEquals(
            SemanticSearchStatus.UNSUPPORTED,
            state(enabled = true, availability = ModelAvailability.UNAVAILABLE)
        )
        assertEquals(
            SemanticSearchStatus.UNSUPPORTED,
            state(enabled = false, availability = ModelAvailability.UNAVAILABLE)
        )
    }

    @Test
    fun `turning it off while the model is missing still reads as off`() {
        assertEquals(
            SemanticSearchStatus.OFF,
            state(enabled = false, availability = ModelAvailability.DOWNLOADABLE)
        )
    }

    @Test
    fun `only the state that is waiting on a download can start one`() {
        assertEquals(true, SemanticSearchStatus.NEEDS_DOWNLOAD.canStartDownload)
        listOf(
            SemanticSearchStatus.OFF,
            SemanticSearchStatus.ON,
            SemanticSearchStatus.DOWNLOADING,
            SemanticSearchStatus.UNSUPPORTED
        ).forEach { assertEquals(it.name, false, it.canStartDownload) }
    }

    @Test
    fun `a phone that cannot run the model cannot be toggled either`() {
        assertEquals(false, SemanticSearchStatus.UNSUPPORTED.canToggle)
        listOf(
            SemanticSearchStatus.OFF,
            SemanticSearchStatus.ON,
            SemanticSearchStatus.NEEDS_DOWNLOAD,
            SemanticSearchStatus.DOWNLOADING
        ).forEach { assertEquals(it.name, true, it.canToggle) }
    }

    @Test
    fun `search only actually runs when it is on and ready`() {
        assertEquals(true, SemanticSearchStatus.ON.isSearching)
        listOf(
            SemanticSearchStatus.OFF,
            SemanticSearchStatus.NEEDS_DOWNLOAD,
            SemanticSearchStatus.DOWNLOADING,
            SemanticSearchStatus.UNSUPPORTED
        ).forEach { assertEquals(it.name, false, it.isSearching) }
    }

    @Test
    fun `a check that failed is not the same answer as a phone that cannot`() {
        assertEquals(
            SemanticSearchStatus.UNKNOWN,
            state(enabled = false, availability = ModelAvailability.UNKNOWN)
        )
        assertEquals(
            SemanticSearchStatus.UNKNOWN,
            state(enabled = true, availability = ModelAvailability.UNKNOWN)
        )
    }

    @Test
    fun `only the system saying so reports the phone as unsupported`() {
        assertEquals(
            SemanticSearchStatus.UNSUPPORTED,
            state(enabled = false, availability = ModelAvailability.UNAVAILABLE)
        )
        assertNotEquals(
            "a failed check must never claim the phone cannot run the model",
            SemanticSearchStatus.UNSUPPORTED,
            state(enabled = false, availability = ModelAvailability.UNKNOWN)
        )
    }

    @Test
    fun `neither unknown nor unsupported offers a switch or a download`() {
        listOf(SemanticSearchStatus.UNKNOWN, SemanticSearchStatus.UNSUPPORTED).forEach { status ->
            assertEquals("$status should not offer a switch", false, status.canToggle)
            assertEquals("$status should not offer a download", false, status.canStartDownload)
            assertEquals("$status should not claim to be searching", false, status.isSearching)
        }
    }
}
