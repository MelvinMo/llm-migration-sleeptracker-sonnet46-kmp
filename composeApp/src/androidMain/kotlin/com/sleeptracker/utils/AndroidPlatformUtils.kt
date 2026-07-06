package com.sleeptracker.utils

import android.content.Intent
import android.net.Uri
import com.sleeptracker.data.local.applicationContext

// MIGRATION: `Linking.openURL(url)` from React Native → Android Intent.ACTION_VIEW
actual fun openUrl(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    applicationContext.startActivity(intent)
}

actual val isAndroid: Boolean = true
actual val isIos: Boolean = false

// MIGRATION: `Platform.OS === 'android' ? StatusBar.currentHeight : 0`
// Android status bar height is obtained from resources at runtime; 24dp is a safe default.
actual val statusBarHeight: Int
    get() {
        val resourceId = applicationContext.resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        return if (resourceId > 0)
            applicationContext.resources.getDimensionPixelSize(resourceId)
        else 0
    }
