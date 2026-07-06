package com.sleeptracker.presentation.ui.screens.sleep

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Icon
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.config.TransparencyConfig
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.presentation.ui.components.transparency.PrivacyDataPage
import com.sleeptracker.presentation.ui.components.transparency.PrivacyIcon
import com.sleeptracker.presentation.ui.components.transparency.PrivacyTooltip
import com.sleeptracker.presentation.viewmodel.SleepUiState
import com.sleeptracker.presentation.viewmodel.SleepViewModel
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.utils.formatPrivacyViolations
import com.sleeptracker.utils.getPrivacyRiskColor
import com.sleeptracker.utils.getPrivacyRiskIcon
import com.sleeptracker.utils.getPrivacyRiskLabel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.sleep_duration_wheel

// MIGRATION: TypeScript `(tabs)/sleep/index.tsx` → `SleepScreen.kt` Composable.
// `useEffect(() => { loadJournalData() }, [])` → `LaunchedEffect(Unit) { viewModel.loadTodayJournal() }`
// `useState` for modals → `remember { mutableStateOf(false) }`
// `useTransparencyStore` → `transparencyViewModel.journal.collectAsState()`
// `useRouter().push('/(tabs)/sleep/sleep-mode')` → `onNavigateToSleepMode()` callback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    sleepViewModel: SleepViewModel,
    transparencyViewModel: TransparencyViewModel,
    onNavigateToSleepMode: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    onNavigateToConsentPreferences: () -> Unit
) {
    val uiState by sleepViewModel.uiState.collectAsState()
    val journalTransparency by transparencyViewModel.journal.collectAsState()

    // MIGRATION: `useState(false)` → `remember { mutableStateOf(false) }`
    var isBedtimeModalVisible by remember { mutableStateOf(false) }
    var isAlarmModalVisible by remember { mutableStateOf(false) }

    // MIGRATION: `displayNormalUI` — toggles between normal UI and PrivacySleepPage
    var displayNormalUI by remember { mutableStateOf(true) }

    // MIGRATION: `useEffect(() => { loadJournalData() }, [])` → LaunchedEffect
    LaunchedEffect(Unit) {
        sleepViewModel.loadTodayJournal()
    }

    when (uiState) {
        is SleepUiState.Loading -> {
            // MIGRATION: `<Loader size="large"/>` → `CircularProgressIndicator`
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Accent)
            }
        }

        is SleepUiState.Error -> {
            Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
                Text((uiState as SleepUiState.Error).message, color = Color.Red)
            }
        }

        is SleepUiState.Loaded -> {
            val state = uiState as SleepUiState.Loaded
            val bedtime   = state.bedtime.ifEmpty { "Set Time" }
            val alarmTime = state.alarmTime.ifEmpty { "Set Time" }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColors.Background)
                    .padding(horizontal = 20.dp)
            ) {
                // ── Header ────────────────────────────────────────────────────
                // MIGRATION: `<View style={styles.headerContainer}>` →
                // `Row(horizontalArrangement = Arrangement.SpaceBetween)`
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp, bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Sleep Tracker",
                        color      = AppColors.White,
                        fontSize   = 30.sp,
                        fontWeight = FontWeight.Bold,
                        modifier   = Modifier.weight(1f)
                    )

                    // MIGRATION: TRANSPARENCY_UI_CONFIG.sleepPageTooltipEnabled ? <PrivacyTooltip> : <PrivacyIcon>
                    if (TransparencyConfig.UI.sleepPageTooltipEnabled) {
                        PrivacyTooltip(
                            color                  = getPrivacyRiskColor(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            iconSize               = 50f,
                            iconName               = getPrivacyRiskIcon(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            violationsDetected     = getPrivacyRiskLabel(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            privacyViolations      = formatPrivacyViolations(journalTransparency),
                            purpose                = journalTransparency.aiExplanation?.why ?: "",
                            storage                = journalTransparency.aiExplanation?.storage ?: "",
                            access                 = journalTransparency.aiExplanation?.access ?: "",
                            privacyPolicySectionLink = journalTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                            regulationLink         = journalTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                            dataType               = "Journal",
                            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                        )
                    } else {
                        PrivacyIcon(
                            iconName = getPrivacyRiskIcon(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            iconSize = 50.dp,
                            isOpen   = !displayNormalUI,
                            onPress  = { displayNormalUI = !displayNormalUI }
                        )
                    }
                }

                if (displayNormalUI || TransparencyConfig.UI.sleepPageTooltipEnabled) {
                    NormalSleepContent(
                        bedtime             = bedtime,
                        alarmTime           = alarmTime,
                        onEditBedtime       = { isBedtimeModalVisible = true },
                        onEditAlarm         = { isAlarmModalVisible = true },
                        onStartSleepSession = { onNavigateToSleepMode() }
                    )
                } else {
                    PrivacyDataPage(
                        transparency              = journalTransparency,
                        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                    )
                }
            }

            // ── Time picker modals ────────────────────────────────────────────
            // MIGRATION: `<TimeModal>` component → Material3 `TimePicker` in `AlertDialog`
            if (isBedtimeModalVisible) {
                TimePickerDialog(
                    label    = "Set Bedtime",
                    onSave   = { time ->
                        sleepViewModel.saveBedtime(time)
                        isBedtimeModalVisible = false
                    },
                    onCancel = { isBedtimeModalVisible = false }
                )
            }

            if (isAlarmModalVisible) {
                TimePickerDialog(
                    label    = "Set Alarm",
                    onSave   = { time ->
                        sleepViewModel.saveAlarm(time)
                        isAlarmModalVisible = false
                    },
                    onCancel = { isAlarmModalVisible = false }
                )
            }
        }
    }
}

// MIGRATION: `NormalSleepPage.tsx` component content → inline Composable
@OptIn(ExperimentalResourceApi::class)
@Composable
private fun NormalSleepContent(
    bedtime: String,
    alarmTime: String,
    onEditBedtime: () -> Unit,
    onEditAlarm: () -> Unit,
    onStartSleepSession: () -> Unit
) {
    var showTimeError by remember { mutableStateOf(false) }

    Column(
        modifier            = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Sleep duration wheel — full-width square (matches RN sleepTrackerContainer aspectRatio:1)
        Image(
            painter            = painterResource(Res.drawable.sleep_duration_wheel),
            contentDescription = "Sleep duration wheel",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(bottom = 30.dp)
        )

        SleepTimeRow(label = "Bedtime", time = bedtime, onClick = onEditBedtime)
        Spacer(Modifier.height(15.dp))
        SleepTimeRow(label = "Alarm", time = alarmTime, onClick = onEditAlarm)
        Spacer(Modifier.height(20.dp))

        Button(
            onClick  = {
                if (bedtime == "Set Time" || alarmTime == "Set Time") {
                    showTimeError = true
                } else {
                    showTimeError = false
                    onStartSleepSession()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = AppColors.Accent),
            shape    = RoundedCornerShape(12.dp)
        ) {
            Text("SLEEP NOW", color = AppColors.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }

        if (showTimeError) {
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Please set your bedtime and alarm time before starting.",
                color    = Color(0xFFFF6B6B),
                fontSize = 14.sp
            )
        }

        Spacer(Modifier.height(30.dp))
    }
}

@Composable
private fun SleepTimeRow(label: String, time: String, onClick: () -> Unit) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F2E), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, color = AppColors.White, fontSize = 18.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(
            text     = time,
            color    = AppColors.White.copy(alpha = 0.8f),
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        Icon(
            imageVector        = Icons.Filled.Create,
            contentDescription = "Edit $label",
            tint               = AppColors.White,
            modifier           = Modifier.size(20.dp)
        )
    }
}

// MIGRATION: `TimeModal.tsx` → AlertDialog with Material3 TimePicker
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    label: String,
    onSave: (String) -> Unit,
    onCancel: () -> Unit
) {
    val timePickerState = rememberTimePickerState(is24Hour = false)

    AlertDialog(
        onDismissRequest = onCancel,
        title            = { Text(label, color = AppColors.White) },
        text             = { TimePicker(state = timePickerState) },
        confirmButton    = {
            Button(onClick = {
                val hour   = timePickerState.hour
                val minute = timePickerState.minute
                val amPm   = if (hour < 12) "AM" else "PM"
                val h12    = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                // MIGRATION: Format time as "HH:MM AM/PM" matching TypeScript TimeModal output
                onSave(String.format("%02d:%02d %s", h12, minute, amPm))
            }) { Text("Save") }
        },
        dismissButton    = { Button(onClick = onCancel) { Text("Cancel") } },
        containerColor   = Color(0xFF1F1F2E)
    )
}
