package com.sleeptracker.utils

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

// MIGRATION: `Linking.openURL(url)` → iOS `UIApplication.sharedApplication().openURL()`
actual fun openUrl(url: String) {
    NSURL.URLWithString(url)?.let { nsUrl ->
        UIApplication.sharedApplication.openURL(nsUrl)
    }
}

actual val isAndroid: Boolean = false
actual val isIos: Boolean = true
actual val statusBarHeight: Int = 0  // iOS uses SafeArea insets via Compose, not raw pixel height
