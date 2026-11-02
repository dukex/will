package tech.benedicta.will.infrastructure.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.benedicta.will.task.TaskOrigin

@Database(entities = [TaskEntity::class], version = 8, exportSchema = false)
abstract class WillDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile private var instance: WillDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN isActive INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN notificationKey TEXT DEFAULT NULL")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN conversationKey TEXT DEFAULT NULL")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_conversationKey ON tasks (conversationKey)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN unreadCount INTEGER NOT NULL DEFAULT 1")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN shortcutId TEXT DEFAULT NULL")
            }
        }

        // Status is now derived from (startedAt, pausedAt, completedAt) instead
        // of an isActive flag. Old rows that were started-but-not-active become
        // PAUSED; the flag column is dropped via table recreation because
        // SQLite ALTER TABLE DROP COLUMN is unavailable on minSdk 24.
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN pausedAt INTEGER DEFAULT NULL")
                db.execSQL(
                    "UPDATE tasks SET pausedAt = startedAt " +
                    "WHERE isActive = 0 AND startedAt IS NOT NULL AND completedAt IS NULL"
                )
                db.execSQL("""
                    CREATE TABLE tasks_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        text TEXT NOT NULL,
                        sourcePackage TEXT NOT NULL,
                        createdAt INTEGER NOT NULL,
                        startedAt INTEGER,
                        pausedAt INTEGER,
                        completedAt INTEGER,
                        contextSwitchCount INTEGER NOT NULL DEFAULT 0,
                        notificationKey TEXT,
                        conversationKey TEXT,
                        unreadCount INTEGER NOT NULL DEFAULT 1,
                        shortcutId TEXT
                    )
                """.trimIndent())
                db.execSQL("""
                    INSERT INTO tasks_new (
                        id, title, text, sourcePackage, createdAt, startedAt,
                        pausedAt, completedAt, contextSwitchCount,
                        notificationKey, conversationKey, unreadCount, shortcutId
                    )
                    SELECT
                        id, title, text, sourcePackage, createdAt, startedAt,
                        pausedAt, completedAt, contextSwitchCount,
                        notificationKey, conversationKey, unreadCount, shortcutId
                    FROM tasks
                """.trimIndent())
                db.execSQL("DROP TABLE tasks")
                db.execSQL("ALTER TABLE tasks_new RENAME TO tasks")
                db.execSQL(
                    "CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_conversationKey " +
                    "ON tasks (conversationKey)"
                )
            }
        }

        // Before this column, `sourcePackage` carried the origin through one
        // magic value: "manual" for what the user typed, a real package name
        // for everything intercepted from a notification.
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN origin TEXT NOT NULL " +
                    "DEFAULT '${TaskEntity.DEFAULT_ORIGIN}'"
                )
                db.execSQL(
                    "UPDATE tasks SET origin = '${TaskOrigin.MANUAL.name}' " +
                    "WHERE sourcePackage = '${TaskOrigin.MANUAL_SOURCE_PACKAGE}'"
                )
            }
        }

        fun getInstance(context: Context): WillDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WillDatabase::class.java,
                    "will.db"
                ).addMigrations(
                    MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4,
                    MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                    MIGRATION_7_8
                ).build().also { instance = it }
            }
    }
}
