package tech.benedicta.will.provider

/**
 * A task as an external provider reports it.
 *
 * Built through [of], which refuses anything a provider should not have sent.
 * Nothing coming back over a network is assumed to be well formed, and a row
 * with no id or no title is not something the feed can show or act on.
 */
data class ProviderTask(
    val remoteId: String,
    val title: String,
    val isCompleted: Boolean
) {
    companion object {
        fun of(remoteId: String, title: String, isCompleted: Boolean): ProviderTask? {
            val id = remoteId.trim()
            val cleanTitle = title.trim()
            if (id.isEmpty() || cleanTitle.isEmpty()) return null
            return ProviderTask(remoteId = id, title = cleanTitle, isCompleted = isCompleted)
        }
    }
}

/**
 * Somewhere other than this phone that holds the user's tasks.
 *
 * The feed never reads this. It reads the local store, which a provider
 * mirrors into, so the home screen cannot hang, blank or crash because a
 * network call was slow or failed. That is the same shape the model work
 * settled on: the slow thing only ever catches up.
 *
 * What may be sent is not this interface's decision. [ProviderSharing] answers
 * that, and it answers it before a payload is ever built.
 */
interface TaskProvider {

    /** Stable and lowercase, stored on a task so it syncs back to its own provider. */
    val id: String

    suspend fun openTasks(): List<ProviderTask>

    /** The remote id of what was created, or null when it could not be. */
    suspend fun create(payload: ProviderPayload): String?

    suspend fun update(remoteId: String, payload: ProviderPayload)

    suspend fun complete(remoteId: String)

    suspend fun delete(remoteId: String)

    companion object {
        /**
         * What every install has until the user connects something, so the
         * rest of the code never branches on whether a provider exists.
         */
        val None: TaskProvider = object : TaskProvider {
            override val id = ""
            override suspend fun openTasks(): List<ProviderTask> = emptyList()
            override suspend fun create(payload: ProviderPayload): String? = null
            override suspend fun update(remoteId: String, payload: ProviderPayload) = Unit
            override suspend fun complete(remoteId: String) = Unit
            override suspend fun delete(remoteId: String) = Unit
        }
    }
}
