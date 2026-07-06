package com.sleeptracker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.model.DefaultTransparencyEvents
import com.sleeptracker.model.GeneralSleepData
import com.sleeptracker.model.JournalData
import com.sleeptracker.model.TransparencyEvent
import com.sleeptracker.network.TransparencyApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

// MIGRATION: Sleep tab screen logic was inline in (tabs)/sleep/index.tsx and sleep-mode.tsx.
// Extracted to SleepViewModel following MVVM pattern.
// Business logic removed from Composables keeps them testable and lifecycle-safe.

sealed class SleepUiState {
    data object Loading : SleepUiState()
    data class Loaded(
        val bedtime: String,
        val alarmTime: String,
        val journalTransparency: TransparencyEvent
    ) : SleepUiState()
    data class Error(val message: String) : SleepUiState()
}

sealed class SleepModeUiState {
    data object Idle : SleepModeUiState()
    data class Active(
        val currentTime: String,
        val alarmTime: String,
        val pressDuration: Int,    // ms held (0–2000)
    ) : SleepModeUiState()
    data object WakingUp : SleepModeUiState()
}

class SleepViewModel(
    private val journalRepository: JournalRepository,
    private val transparencyApiService: TransparencyApiService,
    private val transparencyViewModel: TransparencyViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow<SleepUiState>(SleepUiState.Loading)
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    fun loadTodayJournal() {
        viewModelScope.launch {
            _uiState.value = SleepUiState.Loading
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                val journal = journalRepository.getJournalByDate(today)
                _uiState.value = SleepUiState.Loaded(
                    bedtime  = journal?.bedtime ?: "",
                    alarmTime = journal?.alarmTime ?: "",
                    journalTransparency = transparencyViewModel.journal.value
                )
            } catch (e: Exception) {
                _uiState.value = SleepUiState.Error("Failed to load journal: ${e.message}")
            }
        }
    }

    // MIGRATION: `saveBedTimeToJournal` from sleep/index.tsx
    // Fire-and-forget transparency analysis (same pattern as TypeScript .then().catch())
    fun saveBedtime(newBedtime: String) {
        viewModelScope.launch {
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                val transparencyEvent = DefaultTransparencyEvents.JOURNAL
                transparencyViewModel.setJournalTransparency(transparencyEvent)

                val result = journalRepository.editJournal(
                    journal = JournalData(
                        date = today, userId = "", journalId = "",
                        bedtime = newBedtime, alarmTime = "", sleepDuration = "",
                        diaryEntry = "", sleepNotes = emptyList()
                    ),
                    date = today
                )

                // Non-blocking transparency analysis (fire-and-forget, same as TS)
                viewModelScope.launch {
                    runCatching {
                        val updated = transparencyApiService.analyzePrivacyRisks(transparencyEvent)
                        transparencyViewModel.setJournalTransparency(updated)
                    }
                }

                val current = _uiState.value as? SleepUiState.Loaded
                _uiState.value = current?.copy(bedtime = result?.bedtime ?: newBedtime)
                    ?: SleepUiState.Loaded(newBedtime, "", transparencyEvent)

            } catch (e: Exception) {
                _uiState.value = SleepUiState.Error("Failed to save bedtime: ${e.message}")
            }
        }
    }

    // MIGRATION: `generalSleepDataRepository.createSleepData(data)` from questions.tsx
    // → fire-and-forget HTTP POST via httpClient. Wired in AppNavigation via sleepViewModel.saveGeneralSleepData.
    // MIGRATION_FLAG: Implement GeneralSleepDataRepository with a POST /general-sleep-data/ endpoint.
    // Until then, this logs the data locally; no HTTP call is made.
    fun saveGeneralSleepData(data: GeneralSleepData) {
        viewModelScope.launch {
            runCatching {
                // MIGRATION_FLAG: Replace with: httpClient.post("/general-sleep-data/", data)
                // The TypeScript called: await generalSleepDataRepository.createSleepData(sleepData)
                // Requires a GeneralSleepDataRepository backed by HttpApiClient.
            }
        }
    }

    fun saveAlarm(newAlarm: String) {
        viewModelScope.launch {
            try {
                val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
                val transparencyEvent = DefaultTransparencyEvents.JOURNAL
                transparencyViewModel.setJournalTransparency(transparencyEvent)

                val result = journalRepository.editJournal(
                    journal = JournalData(
                        date = today, userId = "", journalId = "",
                        bedtime = "", alarmTime = newAlarm, sleepDuration = "",
                        diaryEntry = "", sleepNotes = emptyList()
                    ),
                    date = today
                )

                viewModelScope.launch {
                    runCatching {
                        val updated = transparencyApiService.analyzePrivacyRisks(transparencyEvent)
                        transparencyViewModel.setJournalTransparency(updated)
                    }
                }

                val current = _uiState.value as? SleepUiState.Loaded
                _uiState.value = current?.copy(alarmTime = result?.alarmTime ?: newAlarm)
                    ?: SleepUiState.Loaded("", newAlarm, transparencyEvent)

            } catch (e: Exception) {
                _uiState.value = SleepUiState.Error("Failed to save alarm: ${e.message}")
            }
        }
    }
}
