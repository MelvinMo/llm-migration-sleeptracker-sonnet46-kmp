package com.sleeptracker.storage

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

// MIGRATION: TypeScript `AsyncStorage` → Android `DataStore<Preferences>`.
// DataStore is the modern Android replacement for SharedPreferences and AsyncStorage.
// It provides coroutine-based async access with Flow support.
// Non-sensitive data only — sensitive data uses AndroidSecureStorage.

private val Context.dataStore by preferencesDataStore(name = "app_preferences")

actual class AppDataStore(private val context: Context) {

    actual suspend fun setString(key: String, value: String) {
        context.dataStore.edit { prefs ->
            prefs[stringPreferencesKey(key)] = value
        }
    }

    actual suspend fun getString(key: String): String? {
        val prefs = context.dataStore.data.first()
        return prefs[stringPreferencesKey(key)]
    }

    actual suspend fun remove(key: String) {
        context.dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(key))
        }
    }
}
