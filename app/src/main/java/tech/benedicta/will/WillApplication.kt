package tech.benedicta.will

import android.app.Application
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import tech.benedicta.will.infrastructure.AndroidPermissionInspector
import tech.benedicta.will.infrastructure.SharedPrefsAppFilterRepository
import tech.benedicta.will.infrastructure.SharedPrefsOnboardingRepository
import tech.benedicta.will.infrastructure.SharedPrefsPinnedAppsRepository
import tech.benedicta.will.infrastructure.room.RoomTaskRepository
import tech.benedicta.will.infrastructure.room.WillDatabase
import tech.benedicta.will.launcher.PinnedAppsRepository
import tech.benedicta.will.notification.AppFilterRepository
import tech.benedicta.will.onboarding.OnboardingRepository
import tech.benedicta.will.onboarding.PermissionInspector
import tech.benedicta.will.task.TaskRepository

data class OverlayEvent(val action: String, val taskId: Long)

class WillApplication : Application() {
    val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(WillDatabase.getInstance(this).taskDao())
    }
    val appFilterRepository: AppFilterRepository by lazy {
        SharedPrefsAppFilterRepository(this)
    }
    val pinnedAppsRepository: PinnedAppsRepository by lazy {
        SharedPrefsPinnedAppsRepository(this)
    }
    val onboardingRepository: OnboardingRepository by lazy {
        SharedPrefsOnboardingRepository(this)
    }
    val permissionInspector: PermissionInspector by lazy {
        AndroidPermissionInspector(this)
    }

    private val _overlayEvents = MutableSharedFlow<OverlayEvent>(
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val overlayEvents = _overlayEvents.asSharedFlow()

    fun emitOverlayEvent(event: OverlayEvent) {
        _overlayEvents.tryEmit(event)
    }
}
