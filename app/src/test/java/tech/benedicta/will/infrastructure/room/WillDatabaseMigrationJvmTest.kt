package tech.benedicta.will.infrastructure.room

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import tech.benedicta.will.task.TaskEvent
import tech.benedicta.will.task.TaskEventType
import tech.benedicta.will.task.TaskOrigin

/**
 * The same replay as `WillDatabaseMigrationTest`, run on the JVM through
 * Robolectric so `./gradlew test` answers it.
 *
 * It is a real upgrade: real SQLite executes the migration SQL, and Room
 * compares every migrated table against its entity when it opens the
 * database, so SQL that does not match the entity fails here rather than on
 * someone's phone. Opening is the assertion.
 *
 * The instrumented copy stays, because it is the same check against the
 * device's own SQLite, which is the one that ships.
 */
@RunWith(RobolectricTestRunner::class)
class WillDatabaseMigrationJvmTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val name = "migration-jvm-test.db"

    @Before
    fun removeAnyLeftover() {
        context.deleteDatabase(name)
    }

    @After
    fun clean() {
        context.deleteDatabase(name)
    }

    /** The `tasks` table exactly as version 7 left it. */
    private fun createVersionSeven() {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(name)
                .callback(object : SupportSQLiteOpenHelper.Callback(7) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
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
                        db: SupportSQLiteDatabase,
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
    fun `a database from version seven opens after every migration`() {
        createVersionSeven()

        val database = openWithMigrations()
        val tasks = runBlocking { database.taskDao().getAll().first() }

        assertEquals(2, tasks.size)
        database.close()
    }

    @Test
    fun `the origin column is backfilled from the old magic source package`() {
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
    fun `the task log exists and accepts rows after the upgrade`() {
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
    fun `a task carried over has no derived title until one is derived`() {
        createVersionSeven()

        val database = openWithMigrations()
        val tasks = runBlocking { database.taskDao().getAll().first() }

        tasks.forEach { assertNull("nothing was derived on upgrade", it.toTask().rawTitle) }
        database.close()
    }

    @Test
    fun `the conversation key index survives the upgrade, so dedup still holds`() {
        createVersionSeven()

        val database = openWithMigrations()
        val indexed = database.openHelper.readableDatabase.query(
            "SELECT name FROM sqlite_master WHERE type = 'index' AND tbl_name = 'tasks'"
        ).use { cursor ->
            buildList { while (cursor.moveToNext()) add(cursor.getString(0)) }
        }

        assertEquals(
            "the unique index is what stops one conversation becoming two tasks",
            1,
            indexed.count { it == "index_tasks_conversationKey" }
        )
        database.close()
    }
}
