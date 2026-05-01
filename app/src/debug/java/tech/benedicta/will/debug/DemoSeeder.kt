package tech.benedicta.will.debug

import kotlinx.coroutines.flow.first
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.task.TaskRepository

/**
 * Wipes the local state and applies [buildLucasMorningSeed]. Debug-only —
 * triggered through `DemoSeedReceiver`. Pure suspending API so the same
 * class is exercised in unit tests against in-memory repositories.
 */
class DemoSeeder(
    private val taskRepo: TaskRepository,
    private val pinnedRepo: PinnedAppsRepository,
    private val nowProvider: () -> Long = System::currentTimeMillis
) {
    suspend fun seed() {
        val data = buildLucasMorningSeed(nowProvider())

        taskRepo.deleteAllTasks()

        val priorPins = pinnedRepo.getPinnedPackages().first()
        priorPins.forEach { pinnedRepo.unpin(it) }

        data.tasks.forEach { taskRepo.insertTask(it) }
        data.pinnedPackages.forEach { pinnedRepo.pin(it) }
    }
}
