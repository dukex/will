package tech.benedicta.will.infrastructure.room

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventType
import tech.benedicta.will.task.TaskOrigin

/**
 * Replays a real upgrade from the schema that shipped, which is the one thing
 * a JVM test cannot do: Room validates the migrated tables against the
 * entities when it opens the database, so a migration whose SQL does not match
 * fails here rather than on someone's phone.
 *
 * Run it with: ./gradlew connectedDebugAndroidTest
 */
@RunWith(AndroidJUnit4::class)
class WillDatabaseMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext
    private val name = "migration-test.db"

    @Before
    fun removeAnyLeftover() = context.deleteDatabase(name).let { }

    @After
    fun clean() = context.deleteDatabase(name).let { }

    /** The `tasks` table exactly as version 7 left it. */
    private fun createVersionSeven() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL(
                            """
                            CREATE TABLE tasks (
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
                            """.trimIndent()
                        )
                        db.execSQL(
                            "CREATE UNIQUE INDEX IF NOT EXISTS index_tasks_conversationKey " +
                                "ON tasks (conversationKey)"
                        )
                        db.execSQL(
                            "INSERT INTO tasks (title, text, sourcePackage, createdAt, unreadCount) " +
                                "VALUES ('pagar o boleto', '', 'manual', 1000, 1)"
                        )
                        db.execSQL(
                            "INSERT INTO tasks (title, text, sourcePackage, createdAt, unreadCount) " +
                                "VALUES ('Maria', 'oi', 'com.whatsapp', 2000, 1)"
                        )
                    }

                    override fun onUpgrade(
                        db: androidx.sqlite.db.SupportSQLiteDatabase,
                        oldVersion: Int,
                        newVersion: Int
                    ) = Unit
                })
                .build()
        )
        helper.writableDatabase.close()
    }

    private fun openWithMigrations(): WillDatabase =
        Room.databaseBuilder(context, WillDatabase::class.java, name)
            .addMigrations(*WillDatabase.MIGRATIONS)
            .build()

    @Test
    fun a_database_from_version_seven_opens_after_every_migration() {
        createVersionSeven()

        val database = openWithMigrations()
        // Opening is the assertion: Room compares every migrated table with
        // the entity and throws when they differ.
        val tasks = runBlocking { database.taskDao().getAll().first() }

        assertEquals(2, tasks.size)
        database.close()
    }

    @Test
    fun the_origin_column_is_backfilled_from_the_old_magic_source_package() {
        createVersionSeven()

        val database = openWithMigrations()
        val tasks = runBlocking { database.taskDao().getAll().first() }

        assertEquals(
            TaskOrigin.MANUAL,
            tasks.single { it.sourcePackage == "manual" }.toTask().origin
        )
        assertEquals(
            TaskOrigin.NOTIFICATION,
            tasks.single { it.sourcePackage == "com.whatsapp" }.toTask().origin
        )
        database.close()
    }

    @Test
    fun the_task_log_exists_and_accepts_rows_after_the_upgrade() {
        createVersionSeven()

        val database = openWithMigrations()
        val taskId = runBlocking { database.taskDao().getAll().first() }.first().id
        val log = RoomTaskEventRepository(database.taskEventDao())

        runBlocking {
            log.append(TaskEvent(taskId = taskId, type = TaskEventType.CREATED, at = 1L))
            assertEquals(1, log.eventsForTask(taskId).first().size)
        }
        database.close()
    }

    @Test
    fun a_task_carried_over_has_no_derived_title_until_one_is_derived() {
        createVersionSeven()

        val database = openWithMigrations()
        val tasks = runBlocking { database.taskDao().getAll().first() }

        tasks.forEach { assertNull("nothing was derived on upgrade", it.toTask().rawTitle) }
        database.close()
    }
}
