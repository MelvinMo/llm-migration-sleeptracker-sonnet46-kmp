package com.sleeptracker.presentation.ui.screens.journal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.config.TransparencyConfig
import com.sleeptracker.constants.AppColors
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.model.JournalData
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.model.SleepNote
import com.sleeptracker.presentation.ui.components.transparency.PrivacyIcon
import com.sleeptracker.presentation.ui.components.transparency.PrivacyJournalPage
import com.sleeptracker.presentation.ui.components.transparency.PrivacyTooltip
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.utils.formatPrivacyViolations
import com.sleeptracker.utils.getPrivacyRiskColor
import com.sleeptracker.utils.getPrivacyRiskIcon
import com.sleeptracker.utils.getPrivacyRiskIconForPage
import com.sleeptracker.utils.getPrivacyRiskLabel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// MIGRATION: TypeScript `(tabs)/journal.tsx` → `JournalScreen.kt`.
// Layout: ImageBackground header (NOT inside ScrollView) + ScrollView content — same separation here.
// Calendar: RN Calendar.tsx week-row component toggled by clicking the date header.
// PrivacyIcon: positioned absolutely top-right of the header box (matches RN position: 'absolute').
// Background: backgroundColor: 'black'.

@Composable
fun JournalScreen(
    journalRepository: JournalRepository,
    transparencyViewModel: TransparencyViewModel,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    onNavigateToConsentPreferences: () -> Unit
) {
    val journalTransparency       by transparencyViewModel.journal.collectAsState()
    val accelerometerTransparency by transparencyViewModel.accelerometer.collectAsState()

    var journal         by remember { mutableStateOf<JournalData?>(null) }
    var isLoading       by remember { mutableStateOf(true) }
    var displayNormalUI by remember { mutableStateOf(true) }
    var showEntryModal  by remember { mutableStateOf(false) }
    var showNotesModal  by remember { mutableStateOf(false) }
    var showCalendar    by remember { mutableStateOf(false) }
    val scope           = rememberCoroutineScope()

    val todayDate = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    var selectedDate by remember { mutableStateOf(todayDate) }

    // Derived from selectedDate — re-computed on each recomposition when date changes
    val dateString    = selectedDate.toString()
    val month         = selectedDate.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }
    val formattedDate = "$month ${selectedDate.dayOfMonth.toString().padStart(2, '0')}"

    // Reload journal whenever selected date changes
    LaunchedEffect(selectedDate) {
        isLoading = true
        try {
            journal = journalRepository.getJournalByDate(dateString)
        } catch (_: Exception) { }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (isLoading) {
            CircularProgressIndicator(color = AppColors.Accent, modifier = Modifier.align(Alignment.Center))
        } else if (!displayNormalUI) {
            PrivacyJournalPage(
                journalTransparency           = journalTransparency,
                accelerometerTransparency     = accelerometerTransparency,
                onNavigateToPrivacyPolicy     = onNavigateToPrivacyPolicy,
                onNavigateToConsentPreferences = onNavigateToConsentPreferences
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {

                // ── Header (NOT inside scroll — matches RN ImageBackground outside ScrollView) ──
                // MIGRATION: RN has ImageBackground + rgba(0,20,40,0.8) overlay, borderRadius:16.
                // Using background(color, shape) instead of clip() so the calendar content
                // is NOT clipped — RoundedCornerShape is applied only to the background paint.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF001428), RoundedCornerShape(16.dp))
                ) {
                    Column {
                        // Date header — clickable to toggle calendar (matches RN onPress)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCalendar = !showCalendar }
                                .padding(start = 30.dp, end = 30.dp, top = 50.dp, bottom = 20.dp)
                        ) {
                            Column {
                                Text(
                                    text       = if (selectedDate == todayDate) "Today" else formattedDate,
                                    color      = AppColors.White,
                                    fontSize   = 32.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(5.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text  = formattedDate,
                                        color = AppColors.White.copy(alpha = 0.8f),
                                        fontSize = 18.sp
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Icon(
                                        imageVector        = if (showCalendar) Icons.Filled.KeyboardArrowUp
                                                            else Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint               = AppColors.White,
                                        modifier           = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Calendar — shown when showCalendar == true
                        if (showCalendar) {
                            WeekCalendar(
                                selectedDate = selectedDate,
                                onSelectDate = { newDate ->
                                    selectedDate = newDate
                                    showCalendar = false
                                }
                            )
                            Spacer(Modifier.height(16.dp))
                        }
                    }

                    // MIGRATION: PrivacyIcon at position:'absolute', top:50, right:30
                    if (!TransparencyConfig.UI.journalTooltipEnabled) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 50.dp, end = 30.dp)
                        ) {
                            PrivacyIcon(
                                iconName = getPrivacyRiskIconForPage(
                                    listOf(
                                        journalTransparency.privacyRisk ?: PrivacyRisk.LOW,
                                        accelerometerTransparency.privacyRisk ?: PrivacyRisk.LOW
                                    )
                                ),
                                iconSize = 50.dp,
                                isOpen   = !displayNormalUI,
                                onPress  = { displayNormalUI = !displayNormalUI }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Scrollable content (matches RN ScrollView) ────────────────
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // ── Sleep Goal section ────────────────────────────────────
                    Text(
                        text     = "Sleep Goal",
                        color    = AppColors.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 15.dp, start = 10.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                                Column {
                                    Text("🌙 Bedtime", color = AppColors.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = journal?.bedtime?.ifEmpty { "--" } ?: "--",
                                        color      = AppColors.White,
                                        fontSize   = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                Column {
                                    Text("⏰ Alarm", color = AppColors.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text       = journal?.alarmTime?.ifEmpty { "--" } ?: "--",
                                        color      = AppColors.White,
                                        fontSize   = 16.sp,
                                        fontWeight = FontWeight.W500
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("🧭 Goal", color = AppColors.White.copy(alpha = 0.7f), fontSize = 14.sp)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text       = journal?.sleepDuration?.ifEmpty { "--" } ?: "--",
                                    color      = AppColors.White,
                                    fontSize   = 18.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Diary section ─────────────────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(bottom = 15.dp, start = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Diary", color = AppColors.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        if (TransparencyConfig.UI.journalTooltipEnabled) {
                            PrivacyTooltip(
                                color                    = getPrivacyRiskColor(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                iconName                 = getPrivacyRiskIcon(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                violationsDetected       = getPrivacyRiskLabel(journalTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                privacyViolations        = formatPrivacyViolations(journalTransparency),
                                purpose                  = journalTransparency.aiExplanation?.why ?: "",
                                storage                  = journalTransparency.aiExplanation?.storage ?: "",
                                access                   = journalTransparency.aiExplanation?.access ?: "",
                                privacyPolicySectionLink = journalTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                                regulationLink           = journalTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                                dataType                 = "Journal",
                                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                            )
                        }
                    }

                    // Sleep Notes sub-card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Column {
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Text("Sleep Notes", color = AppColors.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                                Icon(
                                    imageVector        = Icons.Filled.AddCircle,
                                    contentDescription = "Add sleep note",
                                    tint               = AppColors.Accent,
                                    modifier           = Modifier.size(24.dp).clickable { showNotesModal = true }
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            val notes = journal?.sleepNotes ?: emptyList()
                            if (notes.isEmpty()) {
                                Text(
                                    "No sleep notes added yet.",
                                    color    = Color(0xFF8E8E93),
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 10.dp).fillMaxWidth()
                                )
                            } else {
                                notes.forEach { note ->
                                    Row(modifier = Modifier.padding(bottom = 5.dp)) {
                                        Text("•", color = AppColors.White, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                        Text(note.name, color = AppColors.White, fontSize = 16.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(15.dp))

                    // Journal entry card (text preview + pencil)
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = journal?.diaryEntry?.ifEmpty { "Write something to record your day... " }
                                       ?: "Write something to record your day... ",
                            color    = AppColors.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            modifier = Modifier
                                .weight(6f)
                                .padding(vertical = 15.dp, horizontal = 20.dp)
                        )
                        Box(
                            modifier         = Modifier
                                .weight(1f)
                                .padding(15.dp)
                                .clickable { showEntryModal = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Create, contentDescription = "Edit diary", tint = AppColors.White, modifier = Modifier.size(24.dp))
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // ── Activity Tracker section ──────────────────────────────
                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(bottom = 15.dp, start = 10.dp, end = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Activity Tracker", color = AppColors.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        if (TransparencyConfig.UI.journalTooltipEnabled) {
                            PrivacyTooltip(
                                color                    = getPrivacyRiskColor(accelerometerTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                iconName                 = getPrivacyRiskIcon(accelerometerTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                violationsDetected       = getPrivacyRiskLabel(accelerometerTransparency.privacyRisk ?: PrivacyRisk.LOW),
                                privacyViolations        = formatPrivacyViolations(accelerometerTransparency),
                                purpose                  = accelerometerTransparency.aiExplanation?.why ?: "",
                                storage                  = accelerometerTransparency.aiExplanation?.storage ?: "",
                                access                   = accelerometerTransparency.aiExplanation?.access ?: "",
                                optOutLink               = "consent-preferences",
                                privacyPolicySectionLink = accelerometerTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                                regulationLink           = accelerometerTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                                dataType                 = "Activity Tracker",
                                onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                                onNavigateToOptOut       = onNavigateToConsentPreferences
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ActivityItem(label = "Steps",    value = "83", unit = "steps")
                            ActivityItem(label = "Calories", value = "83", unit = "kcal")
                        }
                    }

                    Spacer(Modifier.height(40.dp))
                }
            }
        }
    }

    // ── Journal Entry Modal ───────────────────────────────────────────────────
    if (showEntryModal) {
        JournalEntryModal(
            initialText = journal?.diaryEntry ?: "",
            onSave      = { text ->
                scope.launch {
                    try {
                        val entry = (journal ?: JournalData(
                            journalId     = "",
                            userId        = "",
                            date          = dateString,
                            bedtime       = "",
                            alarmTime     = "",
                            sleepDuration = "",
                            diaryEntry    = text,
                            sleepNotes    = emptyList()
                        )).copy(diaryEntry = text)
                        journal = journalRepository.editJournal(entry, dateString)
                    } catch (_: Exception) { }
                    showEntryModal = false
                }
            },
            onDismiss = { showEntryModal = false }
        )
    }

    // ── Sleep Notes Modal ─────────────────────────────────────────────────────
    if (showNotesModal) {
        SleepNotesModal(
            currentNotes = journal?.sleepNotes ?: emptyList(),
            onSave       = { selectedNotes ->
                scope.launch {
                    try {
                        val entry = (journal ?: JournalData(
                            journalId     = "",
                            userId        = "",
                            date          = dateString,
                            bedtime       = "",
                            alarmTime     = "",
                            sleepDuration = "",
                            diaryEntry    = "",
                            sleepNotes    = selectedNotes
                        )).copy(sleepNotes = selectedNotes)
                        journal = journalRepository.editJournal(entry, dateString)
                    } catch (_: Exception) { }
                    showNotesModal = false
                }
            },
            onDismiss = { showNotesModal = false }
        )
    }
}

// ── Week Calendar ─────────────────────────────────────────────────────────────
// MIGRATION: RN `Calendar.tsx` — 7-day week row, S M T W T F S headers,
// selected day in white circle, day numbers in white, selected day text in #001122.

@Composable
private fun WeekCalendar(
    selectedDate: LocalDate,
    onSelectDate: (LocalDate) -> Unit
) {
    val weekDayLabels = listOf("S", "M", "T", "W", "T", "F", "S")

    val startOfWeek: LocalDate = remember(selectedDate) {
        val daysFromSunday = selectedDate.dayOfWeek.value % 7
        LocalDate.fromEpochDays(selectedDate.toEpochDays() - daysFromSunday)
    }
    val days: List<LocalDate> = remember(startOfWeek) {
        (0..6).map { i -> LocalDate.fromEpochDays(startOfWeek.toEpochDays() + i) }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            weekDayLabels.forEach { label ->
                Text(label, color = AppColors.White.copy(alpha = 0.7f), fontSize = 14.sp, fontWeight = FontWeight.W500)
            }
        }
        Spacer(Modifier.height(15.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            days.forEach { day ->
                val isSelected = day == selectedDate
                Box(
                    modifier         = Modifier
                        .size(35.dp)
                        .background(if (isSelected) Color.White else Color.Transparent, CircleShape)
                        .clickable { onSelectDate(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = day.dayOfMonth.toString(),
                        color      = if (isSelected) Color(0xFF001122) else AppColors.White,
                        fontSize   = 16.sp,
                        fontWeight = if (isSelected) FontWeight.W600 else FontWeight.W500
                    )
                }
            }
        }
    }
}

// ── Private composables ───────────────────────────────────────────────────────

@Composable
private fun ActivityItem(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, color = AppColors.White, fontSize = 16.sp, fontWeight = FontWeight.W500)
        Box(
            modifier         = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = AppColors.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text(unit,  color = AppColors.White.copy(alpha = 0.7f), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun JournalEntryModal(
    initialText: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialText) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Journal Entry", color = AppColors.White) },
        text             = {
            OutlinedTextField(
                value         = text,
                onValueChange = { text = it },
                placeholder   = { Text("Write about your sleep...", color = AppColors.LightGrey) },
                modifier      = Modifier.fillMaxWidth().height(160.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = AppColors.Accent,
                    unfocusedBorderColor    = AppColors.InputFieldBackground,
                    focusedTextColor        = AppColors.White,
                    unfocusedTextColor      = AppColors.White,
                    focusedContainerColor   = AppColors.InputFieldBackground,
                    unfocusedContainerColor = AppColors.InputFieldBackground
                )
            )
        },
        confirmButton    = {
            Button(onClick = { onSave(text) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)) {
                Text("Save")
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AppColors.LightGrey) }
        },
        containerColor   = Color(0xFF1F1F2E)
    )
}

@Composable
private fun SleepNotesModal(
    currentNotes: List<SleepNote>,
    onSave: (List<SleepNote>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by remember { mutableStateOf(currentNotes.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title            = { Text("Sleep Notes", color = AppColors.White) },
        text             = {
            Column {
                SleepNote.entries.forEach { note ->
                    Row(
                        modifier          = Modifier
                            .fillMaxWidth()
                            .clickable { selected = if (note in selected) selected - note else selected + note }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked         = note in selected,
                            onCheckedChange = { checked -> selected = if (checked) selected + note else selected - note },
                            colors          = CheckboxDefaults.colors(checkedColor = AppColors.Accent)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(note.name, color = AppColors.White, fontSize = 15.sp)
                    }
                }
            }
        },
        confirmButton    = {
            Button(onClick = { onSave(selected.toList()) }, colors = ButtonDefaults.buttonColors(containerColor = AppColors.Accent)) {
                Text("Save")
            }
        },
        dismissButton    = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = AppColors.LightGrey) }
        },
        containerColor   = Color(0xFF1F1F2E)
    )
}
