package com.sleeptracker.data.local

import android.content.Context
import app.cash.sqldelight.async.coroutines.synchronous
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.sleeptracker.database.SleepTrackerDatabase

// MIGRATION: `expo-sqlite` opened the database with `openDatabaseAsync(dbName)`.
// SQLDelight 2.x uses platform-specific SqlDriver instances.
// Android uses AndroidSqliteDriver which wraps SQLiteOpenHelper.
// MIGRATION_FLAG: SQLCipher was enabled in Expo app (expo-sqlite with useSQLCipher: true).
// SQLDelight's AndroidSqliteDriver does not natively support SQLCipher.
// For SQLCipher support, use sqldelight-sqlcipher-driver instead.
// Since the TypeScript app encrypted field values (not the whole DB), plain SQLite is sufficient
// unless full-database encryption is needed as an additional security layer.

lateinit var applicationContext: Context  // Set in Application.onCreate()

actual fun createSqlDriver(): SqlDriver = AndroidSqliteDriver(
    schema = SleepTrackerDatabase.Schema.synchronous(),
    context = applicationContext,
    name = DATABASE_NAME              // "sleeptracker_data.db" — same filename as TypeScript
)
