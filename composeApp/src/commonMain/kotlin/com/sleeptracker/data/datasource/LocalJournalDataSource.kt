package com.sleeptracker.data.datasource

import com.benasher44.uuid.uuid4
import com.sleeptracker.data.local.LocalDatabaseManager
import com.sleeptracker.data.repository.JournalDataSource
import com.sleeptracker.model.JournalData

// MIGRATION: TypeScript `LocalJournalDataSource.ts` → Kotlin class backed by SQLDelight.
// Replaces expo-sqlite direct queries with SQLDelight type-safe generated functions.

class LocalJournalDataSource(
    private val dbManager: LocalDatabaseManager
) : JournalDataSource {

    override suspend fun getJournalByDate(userId: String, date: String): JournalData? =
        dbManager.getJournalByDate(userId, date)

    // MIGRATION: `editJournal` creates-or-updates a journal entry for the given date.
    // Mirrors TypeScript `INSERT OR REPLACE` + merge logic.
    override suspend fun editJournal(
        date: String,
        journal: JournalData,
        userId: String
    ): JournalData? {
        val existing = dbManager.getJournalByDate(userId, date)
        val journalId = existing?.journalId ?: uuid4().toString()

        // Merge: use existing values for fields not provided in the update
        val merged = JournalData(
            journalId     = journalId,
            userId        = userId,
            date          = date,
            bedtime       = journal.bedtime.takeIf { it.isNotEmpty() } ?: existing?.bedtime ?: "",
            alarmTime     = journal.alarmTime.takeIf { it.isNotEmpty() } ?: existing?.alarmTime ?: "",
            sleepDuration = journal.sleepDuration.takeIf { it.isNotEmpty() } ?: existing?.sleepDuration ?: "",
            diaryEntry    = journal.diaryEntry.takeIf { it.isNotEmpty() } ?: existing?.diaryEntry ?: "",
            sleepNotes    = journal.sleepNotes.takeIf { it.isNotEmpty() } ?: existing?.sleepNotes ?: emptyList()
        )
        dbManager.upsertJournal(merged)
        return merged
    }

    override suspend fun deleteJournal(journalId: String, userId: String) {
        dbManager.deleteJournal(journalId, userId)
    }
}
