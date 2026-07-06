package com.sleeptracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeptracker.model.DefaultTransparencyEvents
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.model.TransparencyEvent
import com.sleeptracker.model.UserConsentPreferences
import com.sleeptracker.storage.AppDataStore
import com.sleeptracker.storage.DataStoreKeys
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// MIGRATION: Zustand `useTransparencyStore` → ViewModel + StateFlow.
// Reason: Zustand is a JS-only state management library with no KMP equivalent.
// ViewModel + StateFlow provides lifecycle-aware reactive state with structured
// coroutine cancellation — the correct pattern for KMP/Compose.
//
// The 6 independent reactive channels (light, microphone, accelerometer, journal,
// sleep, statistics) map to 6 independent `MutableStateFlow<TransparencyEvent>`.
// Each updates atomically via `_stateFlow.value = newEvent` which is thread-safe
// on the main thread dispatcher that Compose observes.

// MIGRATION: UiState sealed class — KMP best practice for screen state.
// Replaces the implicit loading/error states scattered across the Zustand store.
sealed class TransparencyUiState {
    data object Loading : TransparencyUiState()
    data class Loaded(
        val lightSensor: TransparencyEvent,
        val microphone: TransparencyEvent,
        val accelerometer: TransparencyEvent,
        val journal: TransparencyEvent,
        val generalSleep: TransparencyEvent,
        val statistics: TransparencyEvent
    ) : TransparencyUiState()
    data class Error(val message: String) : TransparencyUiState()
}

class TransparencyViewModel(
    private val dataStore: AppDataStore
) : ViewModel() {

    // ─── 6 independent reactive channels ─────────────────────────────────────
    // MIGRATION: Zustand `set({ lightSensorTransparency: event })` →
    // `_lightSensor.value = event`. StateFlow guarantees atomic emission.
    private val _lightSensor     = MutableStateFlow(DefaultTransparencyEvents.LIGHT_SENSOR)
    private val _microphone      = MutableStateFlow(DefaultTransparencyEvents.MICROPHONE)
    private val _accelerometer   = MutableStateFlow(DefaultTransparencyEvents.ACCELEROMETER)
    private val _journal         = MutableStateFlow(DefaultTransparencyEvents.JOURNAL)
    private val _generalSleep    = MutableStateFlow(DefaultTransparencyEvents.GENERAL_SLEEP)
    private val _statistics      = MutableStateFlow(DefaultTransparencyEvents.STATISTICS)

    val lightSensor: StateFlow<TransparencyEvent>   = _lightSensor.asStateFlow()
    val microphone: StateFlow<TransparencyEvent>    = _microphone.asStateFlow()
    val accelerometer: StateFlow<TransparencyEvent> = _accelerometer.asStateFlow()
    val journal: StateFlow<TransparencyEvent>       = _journal.asStateFlow()
    val generalSleep: StateFlow<TransparencyEvent>  = _generalSleep.asStateFlow()
    val statistics: StateFlow<TransparencyEvent>    = _statistics.asStateFlow()

    // ─── Combined UI state for screens that read all channels ─────────────────
    private val _uiState = MutableStateFlow<TransparencyUiState>(TransparencyUiState.Loading)
    val uiState: StateFlow<TransparencyUiState> = _uiState.asStateFlow()

    init {
        // Load persisted transparency events on ViewModel creation.
        // Equivalent to Zustand `loadTransparencyStatus()` called in _layout.tsx useEffect.
        loadTransparencyStatus()
    }

    // MIGRATION: `loadTransparencyStatus: async () => { AsyncStorage.getItem(...) }`
    // → `viewModelScope.launch { dataStore.getString(...) }`.
    // viewModelScope automatically cancels when ViewModel is cleared (lifecycle-aware).
    fun loadTransparencyStatus() {
        viewModelScope.launch {
            try {
                _uiState.value = TransparencyUiState.Loading
                loadEventFromStore(DataStoreKeys.LIGHT_SENSOR_TRANSPARENCY) { _lightSensor.value = it }
                loadEventFromStore(DataStoreKeys.MICROPHONE_TRANSPARENCY) { _microphone.value = it }
                loadEventFromStore(DataStoreKeys.ACCELEROMETER_TRANSPARENCY) { _accelerometer.value = it }
                loadEventFromStore(DataStoreKeys.JOURNAL_TRANSPARENCY) { _journal.value = it }
                loadEventFromStore(DataStoreKeys.STATISTICS_TRANSPARENCY) { _statistics.value = it }
                loadEventFromStore(DataStoreKeys.GENERAL_SLEEP_TRANSPARENCY) { _generalSleep.value = it }

                // Recalculate risk from persisted consent preferences so icons are
                // correct immediately on app start, not just after first toggle.
                val prefsJson = dataStore.getString(DataStoreKeys.USER_CONSENT_PREFERENCES)
                if (prefsJson != null) {
                    try {
                        val prefs = Json.decodeFromString<UserConsentPreferences>(prefsJson)
                        recalculateRisksForConsent(prefs)
                    } catch (_: Exception) { }
                }

                updateCombinedState()
            } catch (e: Exception) {
                _uiState.value = TransparencyUiState.Error("Failed to load transparency status: ${e.message}")
            }
        }
    }

    // ─── Setters ──────────────────────────────────────────────────────────────
    // MIGRATION: `setLightSensorTransparency: async (event) => { set({...}); AsyncStorage.setItem(...) }`
    // → suspend-free public fun that launches a coroutine (fire-and-forget for UI)
    // The StateFlow update is immediate (synchronous), persistence is async but non-blocking.

    fun setLightSensorTransparency(event: TransparencyEvent) {
        _lightSensor.value = event
        persistEvent(DataStoreKeys.LIGHT_SENSOR_TRANSPARENCY, event)
        updateCombinedState()
    }

    fun setMicrophoneTransparency(event: TransparencyEvent) {
        _microphone.value = event
        persistEvent(DataStoreKeys.MICROPHONE_TRANSPARENCY, event)
        updateCombinedState()
    }

    fun setAccelerometerTransparency(event: TransparencyEvent) {
        _accelerometer.value = event
        persistEvent(DataStoreKeys.ACCELEROMETER_TRANSPARENCY, event)
        updateCombinedState()
    }

    fun setJournalTransparency(event: TransparencyEvent) {
        _journal.value = event
        persistEvent(DataStoreKeys.JOURNAL_TRANSPARENCY, event)
        updateCombinedState()
    }

    fun setGeneralSleepTransparency(event: TransparencyEvent) {
        _generalSleep.value = event
        persistEvent(DataStoreKeys.GENERAL_SLEEP_TRANSPARENCY, event)
        updateCombinedState()
    }

    fun setStatisticsTransparency(event: TransparencyEvent) {
        _statistics.value = event
        persistEvent(DataStoreKeys.STATISTICS_TRANSPARENCY, event)
        updateCombinedState()
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private fun persistEvent(key: String, event: TransparencyEvent) {
        viewModelScope.launch {
            try {
                dataStore.setString(key, Json.encodeToString(event))
            } catch (e: Exception) {
                // Non-fatal: transparency event will still update in memory
            }
        }
    }

    private suspend fun loadEventFromStore(key: String, onLoaded: (TransparencyEvent) -> Unit) {
        val stored = dataStore.getString(key) ?: return
        try {
            onLoaded(Json.decodeFromString(stored))
        } catch (_: Exception) {
            // Corrupted/incompatible stored data — silently ignore and use default
        }
    }

    private fun updateCombinedState() {
        _uiState.value = TransparencyUiState.Loaded(
            lightSensor   = _lightSensor.value,
            microphone    = _microphone.value,
            accelerometer = _accelerometer.value,
            journal       = _journal.value,
            generalSleep  = _generalSleep.value,
            statistics    = _statistics.value
        )
    }

    // ─── Local risk recalculation based on consent preferences ──────────────────
    // Called whenever consent preferences change so icons update immediately
    // (mirrors the intent of the RN backend AI analysis, but locally for the prototype).
    fun recalculateRisksForConsent(prefs: UserConsentPreferences) {
        val cloud = prefs.cloudStorageEnabled

        // Risk reflects actual privacy impact of the data flow:
        //   sensor enabled + local only  → LOW  (user consented, data stays on device)
        //   sensor enabled + cloud       → HIGH (audio) / MEDIUM (motion) — data leaves device
        //   sensor disabled              → LOW  (nothing collected)
        val micRisk = when {
            prefs.microphoneEnabled && cloud -> PrivacyRisk.HIGH    // biometric audio to cloud
            else                             -> PrivacyRisk.LOW     // local or disabled
        }
        val accelRisk = when {
            prefs.accelerometerEnabled && cloud -> PrivacyRisk.MEDIUM  // motion data to cloud
            else                                -> PrivacyRisk.LOW     // local or disabled
        }
        val lightRisk = PrivacyRisk.LOW                               // ambient light: never sensitive
        val dataRisk  = if (cloud) PrivacyRisk.MEDIUM else PrivacyRisk.LOW

        _microphone.value    = _microphone.value.copy(privacyRisk    = micRisk)
        _accelerometer.value = _accelerometer.value.copy(privacyRisk = accelRisk)
        _lightSensor.value   = _lightSensor.value.copy(privacyRisk   = lightRisk)
        _journal.value       = _journal.value.copy(privacyRisk       = dataRisk)
        _generalSleep.value  = _generalSleep.value.copy(privacyRisk  = dataRisk)
        _statistics.value    = _statistics.value.copy(privacyRisk    = dataRisk)

        updateCombinedState()
        persistEvent(DataStoreKeys.MICROPHONE_TRANSPARENCY,    _microphone.value)
        persistEvent(DataStoreKeys.ACCELEROMETER_TRANSPARENCY, _accelerometer.value)
        persistEvent(DataStoreKeys.LIGHT_SENSOR_TRANSPARENCY,  _lightSensor.value)
        persistEvent(DataStoreKeys.JOURNAL_TRANSPARENCY,       _journal.value)
        persistEvent(DataStoreKeys.GENERAL_SLEEP_TRANSPARENCY, _generalSleep.value)
        persistEvent(DataStoreKeys.STATISTICS_TRANSPARENCY,    _statistics.value)
    }

    // ─── Equality check (replaces transparencyEventEquality in ExpoSensorService) ──
    fun transparencyEventsAreEqual(a: TransparencyEvent, b: TransparencyEvent): Boolean =
        a.backgroundMode     == b.backgroundMode &&
        a.encryptionMethod   == b.encryptionMethod &&
        a.protocol           == b.protocol &&
        a.storageLocation    == b.storageLocation &&
        a.source             == b.source &&
        a.sensorType         == b.sensorType
}
