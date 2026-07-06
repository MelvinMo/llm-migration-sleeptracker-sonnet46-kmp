package com.sleeptracker.storage

import platform.Foundation.NSUserDefaults

// MIGRATION: TypeScript `AsyncStorage` on iOS → `NSUserDefaults`.
// NSUserDefaults is iOS's standard key-value store for non-sensitive app preferences.
// expo-async-storage used NSUserDefaults (or SQLite) on iOS under the hood.
// Non-sensitive data only — sensitive data uses IosSecureStorage (Keychain).

actual class AppDataStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual suspend fun setString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
        defaults.synchronize()
    }

    actual suspend fun getString(key: String): String? =
        defaults.stringForKey(key)

    actual suspend fun remove(key: String) {
        defaults.removeObjectForKey(key)
        defaults.synchronize()
    }
}
