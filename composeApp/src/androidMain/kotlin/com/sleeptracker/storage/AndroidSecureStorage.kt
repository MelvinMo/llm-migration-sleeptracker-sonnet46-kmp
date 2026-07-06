package com.sleeptracker.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// MIGRATION: TypeScript `expo-secure-store` → Android `EncryptedSharedPreferences`
// backed by Android Keystore hardware-backed key.
// EncryptedSharedPreferences encrypts keys AND values using AES256_GCM / AES256_SIV.
// This is equivalent to expo-secure-store's security guarantees on Android.
//
// MIGRATION: The `SecureStore.setItemAsync('authToken', token)` pattern becomes
// `secureStorage.setItem(SecureStorageKeys.AUTH_TOKEN, token)`.

actual class SecureStorage(private val context: Context) {
    companion object {
        // MIGRATION: Static instance for AndroidEncryptionService to access before DI is set up
        @Volatile var INSTANCE: SecureStorage? = null
    }

    private val masterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",               // filename for encrypted shared prefs
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    actual suspend fun setItem(key: String, value: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().putString(key, value).apply()
        }
    }

    actual suspend fun getItem(key: String): String? {
        return withContext(Dispatchers.IO) {
            sharedPreferences.getString(key, null)
        }
    }

    actual suspend fun deleteItem(key: String) {
        withContext(Dispatchers.IO) {
            sharedPreferences.edit().remove(key).apply()
        }
    }
}
