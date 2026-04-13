package tech.benedicta.will.task

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Task::class], version = 1)
abstract class WillDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var instance: WillDatabase? = null

        fun getInstance(context: Context): WillDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WillDatabase::class.java,
                    "will.db"
                ).build().also { instance = it }
            }
    }
}
