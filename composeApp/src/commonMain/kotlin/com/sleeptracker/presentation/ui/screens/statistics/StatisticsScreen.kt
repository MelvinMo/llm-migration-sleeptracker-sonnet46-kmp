package com.sleeptracker.presentation.ui.screens.statistics

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.constants.AppColors
import com.sleeptracker.data.repository.JournalRepository
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.presentation.ui.components.transparency.PrivacyDataPage
import com.sleeptracker.presentation.ui.components.transparency.PrivacyIcon
import com.sleeptracker.presentation.viewmodel.AuthViewModel
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.utils.getPrivacyRiskIcon
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.sleep_duration_graph
import sleeptracker.composeapp.generated.resources.sleep_quality_daily
import sleeptracker.composeapp.generated.resources.sleep_quality_graph
import sleeptracker.composeapp.generated.resources.sleep_stages_daily

// MIGRATION: TypeScript `(tabs)/statistics.tsx` → `StatisticsScreen.kt`.
// RN layout: ImageBackground header (bg image + rgba(0,20,40,0.8) overlay) with tab pills + PrivacyIcon,
// Calendar component below header when Daily tab active, ScrollView content below.
// Kotlin: Box with sleep_mode_bg image + dark overlay replicates the ImageBackground effect.

@OptIn(ExperimentalResourceApi::class)
@Composable
fun StatisticsScreen(
    journalRepository: JournalRepository,
    authViewModel: AuthViewModel,
    transparencyViewModel: TransparencyViewModel,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit
) {
    val statisticsTransparency by transparencyViewModel.statistics.collectAsState()
    var selectedTab     by remember { mutableStateOf(0) }       // 0 = Daily, 1 = Statistics
    var displayNormalUI by remember { mutableStateOf(true) }

    val todayDate = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }
    var selectedDate by remember { mutableStateOf(todayDate) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Header: dark background + tabs + calendar ───────────────────────────
        // MIGRATION: RN uses ImageBackground (unsplash URL + 0.6 opacity) + rgba(0,20,40,0.8) overlay.
        // Kotlin: solid Color(0xFF001428) is the opaque equivalent of that dark overlay; no image
        // means no unconstrained Image height that would consume all available space.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF001428), RoundedCornerShape(16.dp))
        ) {

            Column {
                // Header row: tab pills (left) + privacy icon (right)
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(top = 50.dp, bottom = 20.dp, start = 30.dp, end = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TabPill(label = "Daily",      isSelected = selectedTab == 0) {
                            selectedTab = 0; displayNormalUI = true
                        }
                        TabPill(label = "Statistics", isSelected = selectedTab == 1) {
                            selectedTab = 1; displayNormalUI = true
                        }
                    }
                    PrivacyIcon(
                        iconName = getPrivacyRiskIcon(statisticsTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        iconSize = 50.dp,
                        isOpen   = !displayNormalUI,
                        onPress  = { displayNormalUI = !displayNormalUI }
                    )
                }

                // Calendar row — visible when Daily tab is active
                if (selectedTab == 0) {
                    WeekCalendar(
                        selectedDate  = selectedDate,
                        onSelectDate  = { selectedDate = it }
                    )
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
        Spacer(Modifier.height(20.dp))

        // ── Scrollable content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            if (displayNormalUI) {
                if (selectedTab == 0) DailyContent()
                else StatsContent()
            } else {
                PrivacyDataPage(
                    transparency              = statisticsTransparency,
                    onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy
                )
            }
            Spacer(Modifier.height(24.dp))
        }
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

    // Find Sunday of the current week (ISO: Monday=1 … Sunday=7; Sunday % 7 == 0)
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
                Text(
                    text       = label,
                    color      = AppColors.White.copy(alpha = 0.7f),
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.W500
                )
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

// ── Daily Tab ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun DailyContent() {
    SectionTitle("Sleep Quality")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter            = painterResource(Res.drawable.sleep_quality_daily),
                contentDescription = "Sleep quality",
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.size(100.dp)
            )
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Time in Bed", color = Color(0xFF888888), fontSize = 14.sp)
                Spacer(Modifier.height(2.dp))
                Text("10:14 PM - 6:44 AM", color = AppColors.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(2.dp))
                Text("8h 30m", color = AppColors.LightGrey, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
                Text("Pretty Good!", color = AppColors.Accent, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    Spacer(Modifier.height(20.dp))

    StatisticItem(label = "Sleep Stages", imageRes = Res.drawable.sleep_stages_daily)
    Spacer(Modifier.height(20.dp))

    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StageItem("Deep Sleep", "21%", "2h 25m", Color(0xFF4A4A4A))
        StageItem("Light Sleep", "56%", "4h 35m", Color(0xFF6A9EFF))
        StageItem("REM",         "17%", "1h 25m", Color(0xFF8A6AFF))
        StageItem("Awake",       "6%",  "30m",    Color(0xFFFA6A4A))
    }
    Spacer(Modifier.height(20.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InsightItem(Icons.Filled.Hotel,     Color(0xFF4A9EFF), "In Bed",       "8h 30 min", Modifier.weight(1f))
        InsightItem(Icons.Filled.NightsStay, Color(0xFF8A6AFF), "Asleep",      "7h 34 min", Modifier.weight(1f))
        InsightItem(Icons.Filled.Timer,     Color(0xFF6A9EFF), "Asleep After", "11 min",    Modifier.weight(1f))
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InsightItem(Icons.AutoMirrored.Filled.VolumeUp, Color(0xFFFFA64A), "Noise",   "39 dB",     Modifier.weight(1f))
        InsightItem(Icons.Filled.Mic,      Color(0xFFFF6B6B), "Snoring", "1h 30 min", Modifier.weight(1f))
        Spacer(Modifier.weight(1f))
    }
    Spacer(Modifier.height(20.dp))

    var activeClipTab by remember { mutableStateOf(0) }
    SectionTitle("Sleep Clips")
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F2E), RoundedCornerShape(16.dp))
            .padding(20.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ClipTab("Snoring", activeClipTab == 0) { activeClipTab = 0 }
            ClipTab("Talking", activeClipTab == 1) { activeClipTab = 1 }
        }
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) { ClipItem(time = "11:04 PM") }
        }
    }
}

// ── Statistics Tab ────────────────────────────────────────────────────────────

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun StatsContent() {
    StatisticItem(label = "Sleep Quality",  imageRes = Res.drawable.sleep_quality_graph)
    Spacer(Modifier.height(4.dp))
    StatisticItem(label = "Sleep Duration", imageRes = Res.drawable.sleep_duration_graph)
    Spacer(Modifier.height(4.dp))
    StatisticItem(label = "Sleep Stages",   imageRes = Res.drawable.sleep_stages_daily)
    Spacer(Modifier.height(4.dp))
    StatisticItem(label = "Snore Time",     imageRes = Res.drawable.sleep_quality_graph)
}

// ── Reusable composables ──────────────────────────────────────────────────────

@Composable
private fun TabPill(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg   = if (isSelected) AppColors.Accent else Color.Transparent
    val text = if (isSelected) AppColors.White  else AppColors.LightGrey
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(label, color = text, fontSize = 18.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text       = title,
        color      = AppColors.White,
        fontSize   = 20.sp,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(bottom = 15.dp, start = 10.dp)
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun StatisticItem(label: String, imageRes: org.jetbrains.compose.resources.DrawableResource) {
    Text(
        text       = label,
        color      = AppColors.White,
        fontSize   = 18.sp,
        fontWeight = FontWeight.SemiBold,
        modifier   = Modifier.padding(bottom = 15.dp)
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1F1F2E), RoundedCornerShape(15.dp))
            .padding(20.dp)
    ) {
        Image(
            painter            = painterResource(imageRes),
            contentDescription = label,
            contentScale       = ContentScale.FillWidth,
            modifier           = Modifier.fillMaxWidth().height(200.dp)
        )
    }
    Spacer(Modifier.height(20.dp))
}

@Composable
private fun StageItem(label: String, percentage: String, duration: String, circleColor: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier            = Modifier.width(76.dp)
    ) {
        Box(
            modifier         = Modifier.size(40.dp).background(circleColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {}
        Spacer(Modifier.height(8.dp))
        Text(label,      color = AppColors.LightGrey, fontSize = 11.sp, textAlign = TextAlign.Center)
        Text(percentage, color = AppColors.White,     fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Text(duration,   color = AppColors.LightGrey, fontSize = 11.sp)
    }
}

@Composable
private fun InsightItem(icon: ImageVector, iconColor: Color, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier
            .background(Color(0xFF1F1F2E), RoundedCornerShape(12.dp))
            .padding(16.dp)
            .height(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.height(8.dp))
        Text(label, color = AppColors.LightGrey, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(4.dp))
        Text(value, color = AppColors.White,     fontSize = 14.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ClipTab(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val bg   = if (isSelected) AppColors.Accent else Color(0xFF333333)
    val text = if (isSelected) AppColors.White  else AppColors.LightGrey
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(label, color = text, fontSize = 14.sp, fontWeight = FontWeight.W500)
    }
}

@Composable
private fun ClipItem(time: String) {
    val barHeights = remember { (0 until 20).map { (5..25).random() } }
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Color(0xFF333333), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Filled.PlayArrow, contentDescription = "Play", tint = AppColors.Accent, modifier = Modifier.size(24.dp))
        Text(time, color = AppColors.White, fontSize = 14.sp, fontWeight = FontWeight.W500, modifier = Modifier.width(60.dp))
        Row(
            modifier              = Modifier.weight(1f).height(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            barHeights.forEach { h ->
                Box(modifier = Modifier.width(2.dp).height(h.dp).background(AppColors.Accent, RoundedCornerShape(1.dp)))
            }
        }
        Icon(Icons.Filled.MoreHoriz, contentDescription = "More", tint = Color(0xFF888888), modifier = Modifier.size(20.dp))
    }
}
