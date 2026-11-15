package tech.benedicta.will.launcher

/**
 * Whether the on-device model is here, could be, or never will be.
 *
 * [UNKNOWN] is not a shade of [UNAVAILABLE]. The system is asked through a
 * call that can throw, and an answer we failed to get says nothing about what
 * the phone can do. Folding the two together is how settings came to tell
 * someone their phone cannot run the model when nothing had established that.
 */
enum class ModelAvailability { UNKNOWN, UNAVAILABLE, DOWNLOADABLE, DOWNLOADING, AVAILABLE }

/**
 * What search by meaning is actually doing, which is what settings has to say.
 *
 * The preference alone cannot answer it: a phone can be willing and unable.
 * Keeping the five states apart is what stops settings from showing "on" while
 * nothing happens, which is the worst of the possible lies.
 */
enum class SemanticSearchStatus(
    val canToggle: Boolean,
    val canStartDownload: Boolean,
    val isSearching: Boolean
) {
    /** The user has not asked for it. */
    OFF(canToggle = true, canStartDownload = false, isSearching = false),

    /** On, the model is here, searches reach it. */
    ON(canToggle = true, canStartDownload = false, isSearching = true),

    /** On, but the model has to be fetched first, and only the user starts that. */
    NEEDS_DOWNLOAD(canToggle = true, canStartDownload = true, isSearching = false),

    /** The fetch is under way; nothing to do but wait. */
    DOWNLOADING(canToggle = true, canStartDownload = false, isSearching = false),

    /** This phone cannot serve the model at all, so there is nothing to turn on. */
    UNSUPPORTED(canToggle = false, canStartDownload = false, isSearching = false),

    /**
     * Asking the system failed, so nothing is known. Offers no switch, because
     * a switch promises something, and claims nothing about the phone.
     */
    UNKNOWN(canToggle = false, canStartDownload = false, isSearching = false);

    companion object {
        fun of(enabled: Boolean, availability: ModelAvailability): SemanticSearchStatus = when {
            availability == ModelAvailability.UNKNOWN -> UNKNOWN
            availability == ModelAvailability.UNAVAILABLE -> UNSUPPORTED
            !enabled -> OFF
            availability == ModelAvailability.AVAILABLE -> ON
            availability == ModelAvailability.DOWNLOADING -> DOWNLOADING
            else -> NEEDS_DOWNLOAD
        }
    }
}
