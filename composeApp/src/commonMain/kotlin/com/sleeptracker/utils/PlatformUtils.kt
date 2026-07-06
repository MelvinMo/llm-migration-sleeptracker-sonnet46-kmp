package com.sleeptracker.utils

// MIGRATION: Platform-specific utilities that are called from commonMain but
// require platform implementations (expect/actual pattern).

// openUrl — used by handleLinkPress() for PIPEDA regulation links
// Android: android.content.Intent(Intent.ACTION_VIEW, uri.toUri())
// iOS:     UIApplication.shared.open(url)
// (Declared here; implementations in androidMain and iosMain)
// Note: Already declared as top-level `expect fun openUrl` in TransparencyUtils.kt
// This file holds additional platform utils.

// MIGRATION: `Platform.OS === 'android'` → expect val
expect val isAndroid: Boolean
expect val isIos: Boolean

// MIGRATION: `StatusBar.currentHeight` (Android) → expect val
expect val statusBarHeight: Int  // pixels; 0 on iOS (handled by SafeArea)
