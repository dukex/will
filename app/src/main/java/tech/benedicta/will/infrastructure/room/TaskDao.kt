package tech.benedicta.will.infrastructure.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert
    suspend fun insert(task: TaskEntity): Long

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAll(): Flow<List<TaskEntity>>

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: Long)

    @Query("UPDATE tasks SET isActive = 0")
    suspend fun clearAllActive()

    @Query("UPDATE tasks SET isActive = 1 WHERE id = :taskId")
    suspend fun setActive(taskId: Long)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
