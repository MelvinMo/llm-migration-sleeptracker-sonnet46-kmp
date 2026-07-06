package com.sleeptracker.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.sleeptracker.constants.AppColors
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.ExperimentalResourceApi
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.SpaceMono_Regular

// MIGRATION: Expo's `_layout.tsx` applied dark theme via `<StatusBar barStyle="light-content">`.
// In Compose, we define a MaterialTheme with a dark color scheme.
//
// MIGRATION: `useFonts({ SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf') })`
// → Compose Resources `Font(Res.font.SpaceMono_Regular)`.
//
// To place the font file:
//   cp frontend/assets/fonts/SpaceMono-Regular.ttf \
//      kotlin_app/composeApp/src/commonMain/composeResources/font/SpaceMono_Regular.ttf
//
// MIGRATION: `Colors.lightBlack` = #181719 → primaryContainer/background
// `Colors.generalBlue` = #39ACE7 → primary accent

private val DarkColorScheme = darkColorScheme(
    primary          = AppColors.Accent,          // #39ACE7 — buttons, active tabs
    onPrimary        = AppColors.White,
    primaryContainer = AppColors.Background,      // #181719
    background       = AppColors.Background,      // #181719 — screen backgrounds
    onBackground     = AppColors.White,
    surface          = Color(0xFF1F1F2E),          // slightly lighter surface for cards
    onSurface        = AppColors.White,
    secondary        = AppColors.LightGrey,
    onSecondary      = AppColors.White,
    error            = AppColors.TooltipRed,
    outline          = AppColors.Grey
)

// MIGRATION: `FontFamily.SpaceMono` replaces `{ SpaceMono: require('../assets/fonts/SpaceMono-Regular.ttf') }`.
// Requires SpaceMono_Regular.ttf in commonMain/composeResources/font/.
@OptIn(ExperimentalResourceApi::class)
@Composable
fun spaceMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.SpaceMono_Regular, weight = FontWeight.Normal, style = FontStyle.Normal)
)

@Composable
fun SleepTrackerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content     = content
    )
}
