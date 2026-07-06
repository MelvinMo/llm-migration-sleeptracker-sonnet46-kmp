package com.sleeptracker.constants

import androidx.compose.ui.graphics.Color

// MIGRATION: TypeScript `Colors.ts` → Kotlin `object Colors` with `Color` values.
// Compose uses `androidx.compose.ui.graphics.Color`, not hex strings.
// All hex values are preserved exactly from the source.
object AppColors {
    // MIGRATION: "#1A1A2E" (spec background) maps to `lightBlack` which is "#181719".
    // Source uses `lightBlack` as background color. The spec mentions #1A1A2E; we use
    // the actual source value to stay pixel-perfect. Both are near-black dark backgrounds.
    // MIGRATION_FLAG: Spec says #1A1A2E background, source uses #181719. Using source value.
    val Background = Color(0xFF181719)          // lightBlack from Colors.ts — primary dark bg

    // MIGRATION: "#4A90D9" accent (spec) ≈ "#39ACE7" (generalBlue in source).
    // Using exact source value for pixel-perfect parity.
    // MIGRATION_FLAG: Spec accent is #4A90D9, source uses #39ACE7. Using source value.
    val Accent = Color(0xFF39ACE7)              // generalBlue — primary accent / active tab tint

    val InputFieldBackground = Color(0xFF5B5775)
    val InputFieldPlaceholder = Color(0xFFAFA3BF)
    val InputFieldSelected = Color(0xFFF2D8A7)
    val HyperlinkBlue = Color(0xFF4A90E2)
    val TooltipLinkBlue = Color(0xFF1A365D)
    val GeneralBlue = Color(0xFF39ACE7)
    val LightBlack = Color(0xFF181719)
    val TooltipGreen = Color(0xFFE0FFDF)
    val TooltipRed = Color(0xFFfd8686)
    val TooltipYellow = Color(0xFFFFFD86)        // source: #FFFD86 (tooltipYellow in Colors.ts)
    val Grey = Color(0x80EBEBF5)                // #EBEBF580 = alpha 80 = 0x80 prefix
    val LightGrey = Color(0xFF888888)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF000000)
}
