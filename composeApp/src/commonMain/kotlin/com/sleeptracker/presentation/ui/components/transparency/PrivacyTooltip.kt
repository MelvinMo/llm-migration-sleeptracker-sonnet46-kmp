package com.sleeptracker.presentation.ui.components.transparency

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.sleeptracker.constants.AppColors
import com.sleeptracker.utils.handleLinkPress

// MIGRATION: TypeScript `PrivacyTooltip.tsx` used `react-native-walkthrough-tooltip` library.
// In Compose, we implement the tooltip natively using `Popup` + `BoxWithConstraints`.
//
// SMART POSITIONING (per hard rules):
//   TypeScript: `iconRef.current.measure((x,y,w,h,pageX,pageY) => { if (pageY > screenHeight/2) → 'top' })`
//   Compose:    `onGloballyPositioned { coords -> coords.positionInWindow() }` gives the same pageY.
//               If positionInWindow().y > screenHeight/2 → show ABOVE the icon.
//               Otherwise → show BELOW the icon.
//
// MIGRATION: `ScrollView` with `nestedScrollEnabled` inside Popup → `Column` with `verticalScroll`
// MIGRATION: `router.push()` navigation → passed as lambda callbacks (navController injected at screen level)

@Composable
fun PrivacyTooltip(
    color: Color,
    iconSize: Float = 40f,
    iconName: String,
    violationsDetected: String,
    privacyViolations: String?,
    purpose: String,
    storage: String,
    access: String,
    optOutLink: String? = null,
    privacyPolicySectionLink: String? = null,
    regulationLink: String? = null,
    dataType: String,
    onNavigateToPrivacyPolicy: (sectionId: String?) -> Unit,
    onNavigateToOptOut: (() -> Unit)? = null,
    isLowRisk: Boolean = violationsDetected == "No Privacy Violations Detected",
    modifier: Modifier = Modifier
) {
    var showTooltip by remember { mutableStateOf(false) }
    // Smart positioning state — tracks icon's vertical position on screen
    var iconPositionY by remember { mutableStateOf(0f) }
    var screenHeightPx by remember { mutableStateOf(1f) }

    // MIGRATION: `Dimensions.get('window').height` → `LocalDensity + BoxWithConstraints`
    // We use onGloballyPositioned to get the actual screen-space Y coordinate of the icon.

    Box(modifier = modifier) {
        BoxWithConstraints {
            screenHeightPx = constraints.maxHeight.toFloat()

            // Icon trigger — positioned using onGloballyPositioned for smart tooltip placement
            val iconModifier = Modifier
                .onGloballyPositioned { coords ->
                    // MIGRATION: `iconRef.current.measure((..., pageY) => ...)` →
                    // `coords.positionInWindow().y` — gives absolute Y in screen pixels
                    iconPositionY = coords.positionInWindow().y
                }

            if (dataType.contains("sensor")) {
                val sensorPart = dataType.split("-")
                val sType  = sensorPart.getOrElse(1) { "accelerometer" }
                val sCloud = dataType.contains("cloud")
                SensorPrivacyIcon(
                    sensorType  = sType,
                    iconName    = iconName,
                    storageType = if (sCloud) "cloud" else "local",
                    onPress     = {
                        showTooltip = !showTooltip
                    },
                    modifier = iconModifier
                )
            } else {
                PrivacyIcon(
                    iconName  = iconName,
                    iconSize  = iconSize.dp,
                    isOpen    = showTooltip,
                    modifier  = iconModifier,
                    onPress   = { showTooltip = !showTooltip }
                )
            }
        }

        // MIGRATION: Tooltip popup — replaces `react-native-walkthrough-tooltip`
        if (showTooltip) {
            Popup(
                onDismissRequest = { showTooltip = false },
                properties = PopupProperties(focusable = true)
            ) {
                // MIGRATION: Smart positioning logic
                // TypeScript: `if (pageY > screenHeight / 2) → placement = 'top'`
                // Compose: calculate offset to show ABOVE or BELOW the icon
                val showAbove = iconPositionY > screenHeightPx / 2

                val tooltipWidth = 320.dp
                val density = LocalDensity.current
                val verticalOffsetDp = if (showAbove) -(48 + 8).dp else (8).dp

                Box(
                    modifier = Modifier
                        .width(tooltipWidth)
                        .offset { IntOffset(0, with(density) { verticalOffsetDp.toPx().toInt() }) }
                ) {
                    TooltipContent(
                        backgroundColor        = color,
                        violationsDetected     = violationsDetected,
                        privacyViolations      = privacyViolations,
                        purpose                = purpose,
                        storage                = storage,
                        access                 = access,
                        isLowRisk              = isLowRisk,
                        privacyPolicySectionLink = privacyPolicySectionLink,
                        regulationLink         = regulationLink,
                        optOutLink             = optOutLink,
                        onClose                = { showTooltip = false },
                        onNavigateToPrivacyPolicy = { section ->
                            showTooltip = false
                            onNavigateToPrivacyPolicy(section)
                        },
                        onNavigateToOptOut     = {
                            showTooltip = false
                            onNavigateToOptOut?.invoke()
                        },
                        onRegulationLinkPress  = { link -> handleLinkPress(link) }
                    )
                }
            }
        }
    }
}

// ─── Tooltip content composable ───────────────────────────────────────────────
// MIGRATION: `renderTooltipContent()` render function → `@Composable` function
@Composable
private fun TooltipContent(
    backgroundColor: Color,
    violationsDetected: String,
    privacyViolations: String?,
    purpose: String,
    storage: String,
    access: String,
    isLowRisk: Boolean,
    privacyPolicySectionLink: String?,
    regulationLink: String?,
    optOutLink: String?,
    onClose: () -> Unit,
    onNavigateToPrivacyPolicy: (String?) -> Unit,
    onNavigateToOptOut: () -> Unit,
    onRegulationLinkPress: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .heightIn(max = 500.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Privacy violations section
        Text(
            text       = violationsDetected,
            fontSize   = 13.sp,
            fontWeight = FontWeight.Bold,
            color      = Color.Black
        )
        if (!isLowRisk && !privacyViolations.isNullOrEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(text = privacyViolations, fontSize = 12.sp, color = Color.Black)
        }

        Spacer(Modifier.height(12.dp))

        // Purpose section
        Text(text = "Purpose:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(Modifier.height(2.dp))
        Text(text = purpose, fontSize = 12.sp, color = Color.Black)

        // Storage and access — only shown for LOW risk (same as TypeScript condition)
        if (isLowRisk) {
            Spacer(Modifier.height(12.dp))
            Text(text = "Storage:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(2.dp))
            Text(text = storage, fontSize = 12.sp, color = Color.Black)

            Spacer(Modifier.height(12.dp))
            Text(text = "Access:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(2.dp))
            Text(text = access, fontSize = 12.sp, color = Color.Black)
        }

        // Links section
        val hasLinks = privacyPolicySectionLink != null || regulationLink != null || optOutLink != null
        if (hasLinks) {
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            if (privacyPolicySectionLink != null) {
                TooltipLinkText("Link to privacy policy section") {
                    onNavigateToPrivacyPolicy(privacyPolicySectionLink)
                }
            }
            if (regulationLink != null) {
                TooltipLinkText("PIPEDA regulation") {
                    onRegulationLinkPress(regulationLink)
                }
            }
            if (optOutLink != null) {
                TooltipLinkText("Opt Out") { onNavigateToOptOut() }
            }
            // "View Full Privacy Policy" is always shown when there are links
            TooltipLinkText("View Full Privacy Policy") {
                onNavigateToPrivacyPolicy(null)
            }
        }
    }
}

@Composable
private fun TooltipLinkText(text: String, onClick: () -> Unit) {
    Text(
        text           = text,
        fontSize       = 12.sp,
        color          = AppColors.TooltipLinkBlue,
        textDecoration = TextDecoration.Underline,
        modifier       = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    )
}

// ─── SensorNotAvailableWidget ─────────────────────────────────────────────────
// MIGRATION: Per hard rules: "Light sensor on iOS: stub gracefully with a SensorNotAvailableWidget."
@Composable
fun SensorNotAvailableWidget(sensorType: String) {
    Box(
        modifier = Modifier
            .background(AppColors.Grey, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text     = "$sensorType sensor not available on this device",
            fontSize = 11.sp,
            color    = AppColors.LightGrey
        )
    }
}
