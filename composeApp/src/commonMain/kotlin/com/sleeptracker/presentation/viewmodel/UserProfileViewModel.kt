package com.sleeptracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.storage.AppDataStore
import com.sleeptracker.storage.DataStoreKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// MIGRATION: Zustand `useProfileStore` → ViewModel + StateFlow.
// Rationale same as AuthViewModel — lifecycle awareness + structured concurrency.

sealed class UserProfileUiState {
    data object Loading : UserProfileUiState()
    data class Loaded(
        val userConsentPreferences: UserConsentPreferences,
        val hasCompletedPrivacyOnboarding: Boolean,
        val hasCompletedAppOnboarding: Boolean
    ) : UserProfileUiState()
    data class Error(val message: String) : UserProfileUiState()
}

class UserProfileViewModel(
    private val dataStore: AppDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    // Convenience accessors for repositories (replaces `useProfileStore.getState().*`)
    val consentPreferences: UserConsentPreferences
        get() = (_uiState.value as? UserProfileUiState.Loaded)?.userConsentPreferences
            ?: UserConsentPreferences.DEFAULT

    val hasCompletedPrivacyOnboarding: Boolean
        get() = (_uiState.value as? UserProfileUiState.Loaded)?.hasCompletedPrivacyOnboarding ?: false

    val hasCompletedAppOnboarding: Boolean
        get() = (_uiState.value as? UserProfileUiState.Loaded)?.hasCompletedAppOnboarding ?: false

    init {
        loadProfileStatus()
    }

    // MIGRATION: `loadProfileStatus: async () => { AsyncStorage.getItem(...) }`
    fun loadProfileStatus() {
        viewModelScope.launch {
            _uiState.value = UserProfileUiState.Loading
            try {
                val privacyOnboarded = dataStore.getString(DataStoreKeys.HAS_COMPLETED_PRIVACY_ONBOARDING) == "true"
                val appOnboarded     = dataStore.getString(DataStoreKeys.HAS_COMPLETED_APP_ONBOARDING) == "true"
                val prefsJson        = dataStore.getString(DataStoreKeys.USER_CONSENT_PREFERENCES)
                val prefs = if (prefsJson != null) {
                    try { Json.decodeFromString<UserConsentPreferences>(prefsJson) }
                    catch (_: Exception) { UserConsentPreferences.DEFAULT }
                } else {
                    UserConsentPreferences.DEFAULT
                }
                _uiState.value = UserProfileUiState.Loaded(prefs, privacyOnboarded, appOnboarded)
            } catch (e: Exception) {
                _uiState.value = UserProfileUiState.Error("Failed to load profile: ${e.message}")
            }
        }
    }

    fun setHasCompletedPrivacyOnboarding(value: Boolean) {
        val current = _uiState.value as? UserProfileUiState.Loaded ?: return
        _uiState.value = current.copy(hasCompletedPrivacyOnboarding = value)
        viewModelScope.launch {
            dataStore.setString(DataStoreKeys.HAS_COMPLETED_PRIVACY_ONBOARDING, if (value) "true" else "false")
        }
    }

    fun setHasCompletedAppOnboarding(value: Boolean) {
        val current = _uiState.value as? UserProfileUiState.Loaded ?: return
        _uiState.value = current.copy(hasCompletedAppOnboarding = value)
        viewModelScope.launch {
            dataStore.setString(DataStoreKeys.HAS_COMPLETED_APP_ONBOARDING, if (value) "true" else "false")
        }
    }

    fun setUserConsentPreferences(preferences: UserConsentPreferences) {
        val current = _uiState.value as? UserProfileUiState.Loaded ?: return
        _uiState.value = current.copy(userConsentPreferences = preferences)
        viewModelScope.launch {
            dataStore.setString(DataStoreKeys.USER_CONSENT_PREFERENCES, Json.encodeToString(preferences))
        }
    }
}
