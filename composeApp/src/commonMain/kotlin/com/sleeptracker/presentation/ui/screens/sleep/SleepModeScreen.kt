package com.sleeptracker.presentation.ui.screens.sleep

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.sleep_mode_bg
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sleeptracker.config.TransparencyConfig
import com.sleeptracker.constants.AppColors
import com.sleeptracker.model.DataDestination
import com.sleeptracker.model.PrivacyRisk
import com.sleeptracker.presentation.ui.components.transparency.PrivacyIcon
import com.sleeptracker.presentation.ui.components.transparency.PrivacyTooltip
import com.sleeptracker.presentation.ui.components.transparency.SensorNotAvailableWidget
import com.sleeptracker.presentation.viewmodel.TransparencyViewModel
import com.sleeptracker.utils.formatPrivacyViolations
import com.sleeptracker.utils.getPrivacyRiskColor
import com.sleeptracker.utils.getPrivacyRiskIcon
import com.sleeptracker.utils.getPrivacyRiskIconForPage
import com.sleeptracker.utils.getPrivacyRiskLabel
import com.sleeptracker.utils.isIos
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// MIGRATION: TypeScript `(tabs)/sleep/sleep-mode.tsx` → `SleepModeScreen.kt`.
// Key translations:
//   `useRef<ReturnType<typeof setTimeout>>` → `rememberUpdatedState + coroutine`
//   `setInterval` for clock → `LaunchedEffect + delay(1000)`
//   `handlePressIn/Out` with `setInterval` for long press → `detectTapGestures`
//   `router.replace('/(tabs)/sleep')` → `onNavigateBack()` callback
//   Background image: TypeScript `<ImageBackground src={...}>` →
//     Compose `Box` with background modifier (image must be in composeResources)
//   MIGRATION_FLAG: sleep-mode-bg.png must be added to commonMain/composeResources/drawable/

// MIGRATION: `<ImageBackground source={require('@/assets/images/sleep-mode-bg.png')}>` →
// Compose Resources `painterResource(Res.drawable.sleep_mode_bg)` layered in a Box.
// Place the file at: commonMain/composeResources/drawable/sleep_mode_bg.png
// (cp frontend/assets/images/sleep-mode-bg.png kotlin_app/composeApp/src/commonMain/composeResources/drawable/sleep_mode_bg.png)
@OptIn(ExperimentalResourceApi::class)
@Composable
fun SleepModeScreen(
    transparencyViewModel: TransparencyViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    onNavigateToConsentPreferences: () -> Unit
) {
    val accelTransparency     by transparencyViewModel.accelerometer.collectAsState()
    val lightTransparency     by transparencyViewModel.lightSensor.collectAsState()
    val micTransparency       by transparencyViewModel.microphone.collectAsState()

    // MIGRATION: `useState('')` for currentTime → `remember + LaunchedEffect`
    var currentTime by remember { mutableStateOf("") }
    var alarmTime   by remember { mutableStateOf("") }

    // MIGRATION: Long-press state (`pressDuration` + `pressIntervalRef`)
    // → `detectTapGestures` with coroutine-based press duration tracking
    var pressDuration    by remember { mutableStateOf(0) }
    var isPressing       by remember { mutableStateOf(false) }
    val requiredDuration = 2000  // 2 seconds in ms

    var displayNormalUI by remember { mutableStateOf(true) }

    // MIGRATION: `setInterval(() => setCurrentTime(...), 1000)` → coroutine loop
    LaunchedEffect(Unit) {
        while (true) {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val hour   = now.hour
            val minute = now.minute
            val amPm   = if (hour < 12) "AM" else "PM"
            val h12    = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            currentTime = String.format("%02d:%02d %s", h12, minute, amPm)
            delay(1000)
        }
    }

    // MIGRATION: `pressIntervalRef` setInterval → coroutine that increments pressDuration
    LaunchedEffect(isPressing) {
        if (isPressing) {
            pressDuration = 0
            while (isPressing && pressDuration < requiredDuration) {
                delay(100)
                pressDuration += 100
                if (pressDuration >= requiredDuration) {
                    // Wake up
                    onNavigateBack()
                    delay(50)
                    onNavigateToStatistics()
                }
            }
        } else {
            pressDuration = 0
        }
    }

    val progress = (pressDuration.toFloat() / requiredDuration).coerceIn(0f, 1f)

    // Outer Box: background image + dark overlay + content layered via stacking
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (fills entire screen, cropped to fit)
        Image(
            painter            = painterResource(Res.drawable.sleep_mode_bg),
            contentDescription = null,
            modifier           = Modifier.fillMaxSize(),
            contentScale       = ContentScale.Crop
        )
        // MIGRATION: RN ImageBackground had no overlay tint, but the dark bg color provided contrast.
        // Adding 45% black overlay to maintain legibility over the background image.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.Black.copy(alpha = 0.45f))
        ) {
        // ── Transparency tooltips / page icon ─────────────────────────────────
        if (!TransparencyConfig.UI.sleepModeTooltipEnabled) {
            // Page-level privacy icon (top-right)
            Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 50.dp, end = 30.dp)) {
                PrivacyIcon(
                    iconName = getPrivacyRiskIconForPage(
                        listOf(
                            accelTransparency.privacyRisk ?: PrivacyRisk.LOW,
                            lightTransparency.privacyRisk ?: PrivacyRisk.LOW,
                            micTransparency.privacyRisk ?: PrivacyRisk.LOW
                        )
                    ),
                    iconSize = 50.dp,
                    isOpen   = !displayNormalUI,
                    onPress  = { displayNormalUI = !displayNormalUI }
                )
            }
        } else {
            // MIGRATION: 3 tooltips: accelerometer (top-left), light (top-right), mic (centered)
            // TypeScript had two Row layouts — replicated with Row + Column
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 50.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Accelerometer tooltip
                    PrivacyTooltip(
                        color              = getPrivacyRiskColor(accelTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        iconName           = getPrivacyRiskIcon(accelTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        violationsDetected = getPrivacyRiskLabel(accelTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        privacyViolations  = formatPrivacyViolations(accelTransparency),
                        purpose            = accelTransparency.aiExplanation?.why ?: "",
                        storage            = accelTransparency.aiExplanation?.storage ?: "",
                        access             = accelTransparency.aiExplanation?.access ?: "",
                        optOutLink         = "consent-preferences",
                        privacyPolicySectionLink = accelTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                        regulationLink     = accelTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                        dataType           = "sensor-accelerometer-${if (accelTransparency.storageLocation == DataDestination.GOOGLE_CLOUD) "cloud" else "local"}",
                        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                        onNavigateToOptOut = onNavigateToConsentPreferences
                    )

                    // Light tooltip — or stub if iOS
                    if (isIos) {
                        SensorNotAvailableWidget(sensorType = "Light")
                    } else {
                        PrivacyTooltip(
                            color              = getPrivacyRiskColor(lightTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            iconName           = getPrivacyRiskIcon(lightTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            violationsDetected = getPrivacyRiskLabel(lightTransparency.privacyRisk ?: PrivacyRisk.LOW),
                            privacyViolations  = formatPrivacyViolations(lightTransparency),
                            purpose            = lightTransparency.aiExplanation?.why ?: "",
                            storage            = lightTransparency.aiExplanation?.storage ?: "",
                            access             = lightTransparency.aiExplanation?.access ?: "",
                            optOutLink         = "consent-preferences",
                            privacyPolicySectionLink = lightTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                            regulationLink     = lightTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                            dataType           = "sensor-light-${if (lightTransparency.storageLocation == DataDestination.GOOGLE_CLOUD) "cloud" else "local"}",
                            onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                            onNavigateToOptOut = onNavigateToConsentPreferences
                        )
                    }
                }

                Spacer(Modifier.height(15.dp))

                // Microphone tooltip — centered
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    PrivacyTooltip(
                        color              = getPrivacyRiskColor(micTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        iconName           = getPrivacyRiskIcon(micTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        violationsDetected = getPrivacyRiskLabel(micTransparency.privacyRisk ?: PrivacyRisk.LOW),
                        privacyViolations  = formatPrivacyViolations(micTransparency),
                        purpose            = micTransparency.aiExplanation?.why ?: "",
                        storage            = micTransparency.aiExplanation?.storage ?: "",
                        access             = micTransparency.aiExplanation?.access ?: "",
                        optOutLink         = "consent-preferences",
                        privacyPolicySectionLink = micTransparency.aiExplanation?.privacyPolicyLink?.firstOrNull(),
                        regulationLink     = micTransparency.aiExplanation?.regulationLink?.firstOrNull(),
                        dataType           = "sensor-microphone-${if (micTransparency.storageLocation == DataDestination.GOOGLE_CLOUD) "cloud" else "local"}",
                        onNavigateToPrivacyPolicy = onNavigateToPrivacyPolicy,
                        onNavigateToOptOut = onNavigateToConsentPreferences
                    )
                }
            }
        }

        // ── Normal UI content ─────────────────────────────────────────────────
        if (displayNormalUI) {
            Column(
                modifier            = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(Modifier.height(1.dp))

                // MIGRATION: `<Text style={styles.currentTimeText}>{currentTime}</Text>`
                Text(
                    text       = currentTime,
                    color      = AppColors.White,
                    fontSize   = 60.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Alarm box
                    Row(
                        modifier              = Modifier
                            .fillMaxWidth(0.9f)
                            .background(Color(0x80000000), RoundedCornerShape(12.dp))
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Text("Alarm", color = AppColors.White.copy(alpha = 0.8f), fontSize = 16.sp)
                        Text(alarmTime, color = AppColors.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(Modifier.height(20.dp))

                    // MIGRATION: Long-press wake up button with progress bar
                    // TypeScript: `<TouchableOpacity onPressIn onPressOut>` with interval
                    // Compose: `detectTapGestures(onPress, onTap)` with `isPressing` state
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(64.dp)
                            .background(AppColors.Accent, RoundedCornerShape(12.dp))
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = { _ ->
                                        isPressing = true
                                        tryAwaitRelease()
                                        isPressing = false
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        // Progress bar overlay
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .height(64.dp)
                                .background(Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .align(Alignment.CenterStart)
                        )
                        Text(
                            text       = if (pressDuration >= requiredDuration) "Releasing..." else "Wake up",
                            color      = AppColors.White,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(40.dp))
                }
            }
        }
        } // closes inner overlay Box
    } // closes outer image Box
}
