package com.sleeptracker.data.repository

import com.sleeptracker.config.TransparencyConfig
import com.sleeptracker.crypto.EncryptionService
import com.sleeptracker.crypto.decryptJournalData
import com.sleeptracker.crypto.encryptJournalData
import com.sleeptracker.model.DataDestination
import com.sleeptracker.model.EncryptionMethod
import com.sleeptracker.model.JournalData
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel

// MIGRATION: TypeScript `JournalDataRepository.ts` → Kotlin class with constructor injection.
// The repository abstracts local vs. cloud data source selection based on user consent preferences.
// Encryption/decryption is delegated to EncryptionService (expect/actual for platform crypto).

interface JournalRepository {
    suspend fun getJournalByDate(date: String): JournalData?
    suspend fun editJournal(journal: JournalData, date: String): JournalData?
    suspend fun deleteJournal(journalId: String)
}

class JournalRepositoryImpl(
    private val cloudDataSource: JournalDataSource,
    private val localDataSource: JournalDataSource,
    private val encryptionService: EncryptionService,
    // MIGRATION: `useProfileStore.getState()` → injected ViewModel read via property
    private val userProfileViewModel: UserProfileViewModel,
    // MIGRATION: `useAuthStore.getState().user` → injected ViewModel read via property
    private val getUserId: () -> String?,
    private val transparencyViewModel: TransparencyViewModel
) : JournalRepository {

    // MIGRATION: `getActiveDataSource()` — selects cloud or local based on cloudStorageEnabled
    private fun getActiveDataSource(): JournalDataSource =
        if (userProfileViewModel.consentPreferences.cloudStorageEnabled) {
            cloudDataSource
        } else {
            localDataSource
        }

    // MIGRATION: `getAuthenticatedUserData()` — throws if not authenticated
    private fun requireUserId(): String =
        getUserId() ?: throw IllegalStateException("User is not authenticated. Please log in first.")

    override suspend fun getJournalByDate(date: String): JournalData? {
        val userId = requireUserId()
        val activeSource = getActiveDataSource()
        val response = activeSource.getJournalByDate(userId, date) ?: return null
        // MIGRATION: `encryptionService.decryptJournalData(response)` → extension function
        return encryptionService.decryptJournalData(response)
    }

    override suspend fun editJournal(journal: JournalData, date: String): JournalData? {
        val userId = requireUserId()
        val activeSource = getActiveDataSource()

        // Update transparency event: cloud vs. local storage destination
        val currentTransparency = transparencyViewModel.journal.value
        val storageLocation = if (activeSource === cloudDataSource)
            DataDestination.GOOGLE_CLOUD else DataDestination.SQLITE_DB

        transparencyViewModel.setJournalTransparency(
            currentTransparency.copy(storageLocation = storageLocation)
        )

        // Determine if we should encrypt based on demo config or real consent
        val encryptAtRest = if (TransparencyConfig.IN_DEMO_MODE) {
            TransparencyConfig.DEMO.encryptedAtRest
        } else {
            true // Always encrypt when not in demo mode
        }

        val dataToStore = journal.copy(userId = userId, date = date)
        val encryptedData = encryptionService.encryptJournalData(dataToStore, encryptAtRest)

        // Update transparency encryption method
        transparencyViewModel.setJournalTransparency(
            transparencyViewModel.journal.value.copy(
                encryptionMethod = if (encryptAtRest) EncryptionMethod.AES_256 else EncryptionMethod.NONE
            )
        )

        val response = activeSource.editJournal(date, encryptedData, userId) ?: return null
        return encryptionService.decryptJournalData(response)
    }

    override suspend fun deleteJournal(journalId: String) {
        val userId = requireUserId()
        getActiveDataSource().deleteJournal(journalId, userId)
    }
}

// ─── Data source interface ────────────────────────────────────────────────────
// MIGRATION: TypeScript `JournalDataSource` interface → Kotlin interface
interface JournalDataSource {
    suspend fun getJournalByDate(userId: String, date: String): JournalData?
    suspend fun editJournal(date: String, journal: JournalData, userId: String): JournalData?
    suspend fun deleteJournal(journalId: String, userId: String)
}
