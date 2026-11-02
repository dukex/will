package tech.benedicta.will.task

/**
 * Outcome of [TaskRepository.upsertByConversationKey].
 *
 * The id alone cannot say what happened: a conversation that already had an
 * open task returns that task's id, which is indistinguishable from a fresh
 * insert. Callers that log creation need the difference, so the upsert states
 * it instead of making them guess.
 */
data class TaskUpsert(val id: Long, val created: Boolean)
