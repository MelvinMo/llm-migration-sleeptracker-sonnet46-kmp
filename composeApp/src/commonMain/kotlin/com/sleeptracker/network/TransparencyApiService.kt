package com.sleeptracker.network

import com.sleeptracker.model.AIPrompt
import com.sleeptracker.model.RegulatoryFramework
import com.sleeptracker.model.TransparencyEvent
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.presentation.viewmodel.UserProfileViewModel
import com.sleeptracker.storage.SecureStorage
import com.sleeptracker.storage.SecureStorageKeys
import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `TransparencyService.ts` → `TransparencyApiService.kt`.
// The TypeScript class imported privacyPolicyData.json and privacyRegulations.json at module level.
// In KMP, JSON files from the frontend are embedded as string resources (see composeApp/commonMain/composeResources/).

// MIGRATION_FLAG: privacyPolicyData.json and privacyRegulations.json must be copied to
// composeApp/src/commonMain/composeResources/files/ and loaded via Compose Resources API.
// For compilation completeness, placeholder strings are used here.

class TransparencyApiService(
    private val httpClient: HttpApiClient,
    private val secureStorage: SecureStorage,
    private val getUserConsentPreferences: () -> UserConsentPreferences,
    private val privacyPolicyJson: String,        // loaded from resources at startup
    private val pipedaRegulationsJson: String     // loaded from resources at startup
) {

    // MIGRATION: `analyzePrivacyRisks(transparencyEvent)` — same signature, now suspend
    // TypeScript: `await this.httpClient.post<{ transparencyEvent: TransparencyEvent }>(...)`
    // Kotlin: `httpClient.post<AnalysisResponse>(...)` with typed response
    suspend fun analyzePrivacyRisks(transparencyEvent: TransparencyEvent): TransparencyEvent {
        val token = getAuthToken()

        val aiPrompt = AIPrompt(
            transparencyEvent     = transparencyEvent,
            privacyPolicy         = privacyPolicyJson,
            userConsentPreferences = getUserConsentPreferences(),
            regulationFrameworks  = listOf(RegulatoryFramework.PIPEDA),
            pipedaRegulations     = pipedaRegulationsJson
        )

        val response = httpClient.post<AnalysisResponse>(
            path  = "/transparency/ai/",
            body  = aiPrompt,
            token = token
        )

        return response.transparencyEvent
    }

    private suspend fun getAuthToken(): String {
        return secureStorage.getItem(SecureStorageKeys.AUTH_TOKEN)
            ?: throw IllegalStateException("Authentication token missing for transparency analysis.")
    }
}

@Serializable
private data class AnalysisResponse(val transparencyEvent: TransparencyEvent)
