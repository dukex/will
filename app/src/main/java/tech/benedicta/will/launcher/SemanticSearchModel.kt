package tech.benedicta.will.launcher

/**
 * The on-device model behind search by meaning, as far as settings needs to
 * know it: whether it is here, and a way to fetch it when it is not.
 *
 * Separate from [SemanticAppSearch] because this is about the model's
 * presence, not about answering a query, and because settings has to ask
 * about it on a phone where searching never happens.
 */
interface SemanticSearchModel {

    suspend fun availability(): ModelAvailability

    /**
     * Fetches the model, which can be megabytes. Only ever called because the
     * user asked for it from settings.
     */
    suspend fun download()

    companion object {
        /** Devices that cannot serve a model at all, which is most of them. */
        val Unsupported = object : SemanticSearchModel {
            override suspend fun availability() = ModelAvailability.UNAVAILABLE
            override suspend fun download() = Unit
        }
    }
}
