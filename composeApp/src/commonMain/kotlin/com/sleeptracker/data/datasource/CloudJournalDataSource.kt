package com.sleeptracker.data.datasource

import com.sleeptracker.data.repository.JournalDataSource
import com.sleeptracker.model.JournalData
import com.sleeptracker.network.HttpApiClient

// MIGRATION: TypeScript `CloudJournalDataSource.ts` → Kotlin class backed by Ktor HttpClient.
// Replaces custom `httpClient.get/post/put/delete` with Ktor-based HttpApiClient.

class CloudJournalDataSource(
    private val httpClient: HttpApiClient,
    private val getToken: () -> String?
) : JournalDataSource {

    private fun requireToken(): String =
        getToken() ?: throw IllegalStateException("Authentication token missing for cloud operation.")

    override suspend fun getJournalByDate(userId: String, date: String): JournalData? {
        val token = requireToken()
        return try {
            httpClient.get<JournalData?>(
                path  = "/phi/journal/date/$date",
                token = token
            )
        } catch (_: Exception) { null }
    }

    override suspend fun editJournal(
        date: String,
        journal: JournalData,
        userId: String
    ): JournalData? {
        val token = requireToken()
        return try {
            httpClient.put<JournalData>(
                path  = "/phi/journal/$date",
                body  = journal,
                token = token
            )
        } catch (_: Exception) { null }
    }

    override suspend fun deleteJournal(journalId: String, userId: String) {
        val token = requireToken()
        httpClient.delete<Unit>(
            path  = "/phi/journal/$journalId",
            token = token
        )
    }
}
