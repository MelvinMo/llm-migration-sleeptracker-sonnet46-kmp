package com.sleeptracker.data.local

import app.cash.sqldelight.db.SqlDriver
import com.sleeptracker.database.SleepTrackerDatabase
import com.sleeptracker.model.AccelerometerSensorData
import com.sleeptracker.model.AmbientNoiseLevel
import com.sleeptracker.model.AudioSensorData
import com.sleeptracker.model.FrequencyBands
import com.sleeptracker.model.JournalData
import com.sleeptracker.model.LightLevel
import com.sleeptracker.model.LightSensorData
import com.sleeptracker.model.MovementIntensity
import com.sleeptracker.model.SensorData
import com.sleeptracker.model.SleepNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// MIGRATION: TypeScript `LocalDatabaseManager` singleton → Kotlin singleton object.
// SQLDelight 2.x generates `SleepTrackerDatabase` from SleepTracker.sq.
// The driver (SqlDriver) is platform-specific (expect/actual) and injected here.

// MIGRATION: The database filename is preserved for data compatibility.
const val DATABASE_NAME = "sleeptracker_data.db"

// MIGRATION: `expect fun createDriver(): SqlDriver` — Android uses AndroidSqliteDriver,
// iOS uses NativeSqliteDriver. Provided in androidMain/iosMain.
expect fun createSqlDriver(): SqlDriver

class LocalDatabaseManager private constructor(driver: SqlDriver) {

    // SQLDelight generates this class from SleepTracker.sq
    private val database = SleepTrackerDatabase(driver)
    private val queries = database.sleepTrackerQueries

    companion object {
        @Volatile private var instance: LocalDatabaseManager? = null

        // MIGRATION: Singleton pattern preserved from TypeScript `LocalDatabaseManager.getInstance()`
        fun getInstance(): LocalDatabaseManager {
            return instance ?: synchronized(this) {
                instance ?: LocalDatabaseManager(createSqlDriver()).also { instance = it }
            }
        }
    }

    // ─── Journal operations ───────────────────────────────────────────────────

    // MIGRATION: `getAll<T>(sql, params)` → typed SQLDelight query function
    suspend fun getJournalByDate(userId: String, date: String): JournalData? =
        withContext(Dispatchers.Default) {
            queries.getJournalByDate(userId = userId, date = date)
                .executeAsOneOrNull()
                ?.toJournalData()
        }

    suspend fun getJournalsByUserId(userId: String): List<JournalData> =
        withContext(Dispatchers.Default) {
            queries.getJournalsByUserId(userId)
                .executeAsList()
                .map { it.toJournalData() }
        }

    // MIGRATION: `executeSql(INSERT OR REPLACE ...)` → SQLDelight `upsertJournal`
    suspend fun upsertJournal(journal: JournalData): Unit =
        withContext(Dispatchers.Default) {
            queries.upsertJournal(
                journalId     = journal.journalId,
                userId        = journal.userId,
                date          = journal.date,
                bedtime       = journal.bedtime,
                alarmTime     = journal.alarmTime,
                sleepDuration = journal.sleepDuration,
                diaryEntry    = journal.diaryEntry,
                sleepNotes    = Json.encodeToString(journal.sleepNotes.map { it.name }),
                createdAt     = Clock.System.now().toString()
            )
        }

    suspend fun updateJournalFields(
        date: String,
        userId: String,
        bedtime: String = "",
        alarmTime: String = "",
        sleepDuration: String = "",
        diaryEntry: String = "",
        sleepNotes: List<SleepNote> = emptyList()
    ): Unit = withContext(Dispatchers.Default) {
        val sleepNotesJson = if (sleepNotes.isNotEmpty())
            Json.encodeToString(sleepNotes.map { it.name })
        else ""
        // Ensure journal row exists before update
        val existing = queries.getJournalByDate(userId, date).executeAsOneOrNull()
        if (existing == null) {
            queries.upsertJournal(
                journalId     = com.benasher44.uuid.uuid4().toString(),
                userId        = userId,
                date          = date,
                bedtime       = bedtime,
                alarmTime     = alarmTime,
                sleepDuration = sleepDuration,
                diaryEntry    = diaryEntry,
                sleepNotes    = if (sleepNotesJson.isEmpty()) "[]" else sleepNotesJson,
                createdAt     = Clock.System.now().toString()
            )
        } else {
            queries.updateJournalFields(
                bedtime       = bedtime,
                alarmTime     = alarmTime,
                sleepDuration = sleepDuration,
                diaryEntry    = diaryEntry,
                sleepNotes    = sleepNotesJson,
                date          = date,
                userId        = userId
            )
        }
    }

    suspend fun deleteJournal(journalId: String, userId: String): Unit =
        withContext(Dispatchers.Default) {
            queries.deleteJournalById(journalId = journalId, userId = userId)
        }

    // ─── Sensor data operations ───────────────────────────────────────────────

    suspend fun insertSensorData(data: SensorData, userId: String): Unit =
        withContext(Dispatchers.Default) {
            when (data) {
                is AudioSensorData -> queries.insertSensorData(
                    id = data.id, userId = userId,
                    timestamp = data.timestamp.toLongOrNull() ?: 0L,
                    date = data.date, sensorType = "audio",
                    averageDecibels = data.averageDecibels,
                    peakDecibels = data.peakDecibels,
                    frequencyBands = Json.encodeToString(data.frequencyBands),
                    audioClipUri = data.audioClipUri,
                    snoreDetected = if (data.snoreDetected) 1L else 0L,
                    ambientNoiseLevel = data.ambientNoiseLevel.name.lowercase(),
                    illuminance = null, lightLevel = null,
                    x = null, y = null, z = null, magnitude = null, movementIntensity = null,
                    createdAt = Clock.System.now().toString()
                )
                is LightSensorData -> queries.insertSensorData(
                    id = data.id, userId = userId,
                    timestamp = data.timestamp.toLongOrNull() ?: 0L,
                    date = data.date, sensorType = "light",
                    averageDecibels = null, peakDecibels = null, frequencyBands = null,
                    audioClipUri = null, snoreDetected = null, ambientNoiseLevel = null,
                    illuminance = data.illuminance,
                    lightLevel = data.lightLevel.name.lowercase(),
                    x = null, y = null, z = null, magnitude = null, movementIntensity = null,
                    createdAt = Clock.System.now().toString()
                )
                is AccelerometerSensorData -> queries.insertSensorData(
                    id = data.id, userId = userId,
                    timestamp = data.timestamp.toLongOrNull() ?: 0L,
                    date = data.date, sensorType = "accelerometer",
                    averageDecibels = null, peakDecibels = null, frequencyBands = null,
                    audioClipUri = null, snoreDetected = null, ambientNoiseLevel = null,
                    illuminance = null, lightLevel = null,
                    x = data.x, y = data.y, z = data.z,
                    magnitude = data.magnitude,
                    movementIntensity = data.movementIntensity.name.lowercase(),
                    createdAt = Clock.System.now().toString()
                )
            }
        }

    // ─── Mapping helpers ──────────────────────────────────────────────────────
    // MIGRATION: SQLDelight returns generated row types; map back to our domain models.

    private fun com.sleeptracker.database.Journals.toJournalData(): JournalData {
        val notes = try {
            val raw = Json.decodeFromString<List<String>>(sleepNotes)
            raw.mapNotNull { s ->
                SleepNote.entries.find { it.name == s || it.name.equals(s, ignoreCase = true) }
            }
        } catch (_: Exception) { emptyList() }

        return JournalData(
            journalId     = journalId,
            userId        = userId,
            date          = date,
            bedtime       = bedtime,
            alarmTime     = alarmTime,
            sleepDuration = sleepDuration,
            diaryEntry    = diaryEntry,
            sleepNotes    = notes
        )
    }
}
