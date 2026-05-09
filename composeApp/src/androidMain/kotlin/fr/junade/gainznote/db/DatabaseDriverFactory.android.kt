package fr.junade.gainznote.db

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {

    private val migrationCallback = object : AndroidSqliteDriver.Callback(GainzNoteDatabase.Schema) {
        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)

            fun safeSql(sql: String) {
                try {
                    db.execSQL(sql)
                } catch (_: Exception) {
                    // Ignore: table/column already exists.
                }
            }

            // Ensure latest schema exists for older installs.
            safeSql(
                """
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER NOT NULL PRIMARY KEY,
                    dark_theme INTEGER NOT NULL DEFAULT 1,
                    black_bg INTEGER NOT NULL DEFAULT 0,
                    chrono_notif_enabled INTEGER NOT NULL DEFAULT 0,
                    ad_free INTEGER NOT NULL DEFAULT 0,
                    language TEXT NOT NULL DEFAULT 'auto',
                    last_workout_type TEXT NOT NULL DEFAULT 'MUSCULATION'
                )
                """.trimIndent()
            )

            safeSql("ALTER TABLE workout ADD COLUMN type TEXT NOT NULL DEFAULT 'MUSCULATION'")
            safeSql("ALTER TABLE exercise ADD COLUMN superset_with TEXT")
            safeSql("ALTER TABLE app_settings ADD COLUMN chrono_notif_enabled INTEGER NOT NULL DEFAULT 0")
            safeSql("ALTER TABLE app_settings ADD COLUMN ad_free INTEGER NOT NULL DEFAULT 0")
            safeSql("ALTER TABLE app_settings ADD COLUMN language TEXT NOT NULL DEFAULT 'auto'")
            safeSql("ALTER TABLE app_settings ADD COLUMN last_workout_type TEXT NOT NULL DEFAULT 'MUSCULATION'")
        }
    }

    actual fun createDriver(): SqlDriver =
        AndroidSqliteDriver(migrationCallback, context, "gainznote.db")
}
