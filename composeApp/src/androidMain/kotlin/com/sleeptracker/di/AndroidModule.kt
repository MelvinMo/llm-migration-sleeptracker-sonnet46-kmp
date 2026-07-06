package com.sleeptracker.di

import com.sleeptracker.crypto.EncryptionService
import com.sleeptracker.storage.AppDataStore
import com.sleeptracker.storage.SecureStorage
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

// MIGRATION: Android-specific Koin module providing Context-dependent implementations.
// Replaces manual construction in MainActivity:
//   AndroidSecureStorage(applicationContext) → SecureStorage(androidContext())
//   AndroidDataStore(applicationContext)     → AppDataStore(androidContext())
//   EncryptionService() + runBlocking { initialize() } → single { ... .apply { ... } }

actual val platformModule: Module = module {
    // MIGRATION: `AndroidSecureStorage(applicationContext)` → Koin `androidContext()`
    // `androidContext()` is set when startKoin { androidContext(this) } is called in MainActivity
    single { SecureStorage(androidContext()) }

    // MIGRATION: `AndroidDataStore(applicationContext)` → Koin `androidContext()`
    single { AppDataStore(androidContext()) }

    // MIGRATION: `EncryptionService().also { runBlocking { it.initialize() } }`
    // Initialization is synchronous (runBlocking) to ensure the AES key is loaded from Keychain
    // before any encrypt/decrypt calls. This runs once at first Koin resolution.
    single {
        EncryptionService().apply {
            runBlocking { initialize() }
        }
    }
}
