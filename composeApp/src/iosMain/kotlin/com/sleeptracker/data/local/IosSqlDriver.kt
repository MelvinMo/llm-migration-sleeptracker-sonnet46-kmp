package com.sleeptracker.data.local

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.sleeptracker.database.SleepTrackerDatabase

// MIGRATION: iOS uses NativeSqliteDriver (SQLDelight Native driver).
// Same database filename preserved for data compatibility.
actual fun createSqlDriver(): SqlDriver = NativeSqliteDriver(
    schema = SleepTrackerDatabase.Schema,
    name = DATABASE_NAME    // "sleeptracker_data.db"
)
