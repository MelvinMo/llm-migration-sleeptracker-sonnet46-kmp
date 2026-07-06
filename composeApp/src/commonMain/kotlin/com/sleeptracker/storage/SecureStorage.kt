package com.sleeptracker.storage

// MIGRATION: TypeScript used `expo-secure-store` for JWT token storage and `AsyncStorage` for
// non-sensitive preferences. In KMP:
//   Sensitive data (JWT, AES key) → Android Keystore-backed EncryptedSharedPreferences / iOS Keychain
//   Non-sensitive preferences (onboarding flags, user JSON) → Android DataStore / iOS NSUserDefaults
//
// This `expect` class covers the SENSITIVE storage tier (replaces expo-secure-store).
// Non-sensitive preferences are handled by `AppDataStore`.

expect class SecureStorage {
    suspend fun setItem(key: String, value: String)
    suspend fun getItem(key: String): String?
    suspend fun deleteItem(key: String)
}

// Key constants — match TypeScript SecureStore key names exactly for migration compatibility
object SecureStorageKeys {
    const val AUTH_TOKEN = "authToken"               // Same as TypeScript SecureStore.setItemAsync('authToken', ...)
    const val ENCRYPTION_KEY = "myAppEncryptionKey"  // Same as EncryptionService.ts ENCRYPTION_KEY_NAME
}
