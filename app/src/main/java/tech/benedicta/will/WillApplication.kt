package tech.benedicta.will

import android.app.Application
import tech.benedicta.will.infrastructure.room.RoomTaskRepository
import tech.benedicta.will.infrastructure.room.WillDatabase
import tech.benedicta.will.task.TaskRepository

class WillApplication : Application() {
    val taskRepository: TaskRepository by lazy {
        RoomTaskRepository(WillDatabase.getInstance(this).taskDao())
    }
}
