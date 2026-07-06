package com.sleeptracker.di

import com.sleeptracker.crypto.EncryptionService
import com.sleeptracker.storage.AppDataStore
import com.sleeptracker.storage.SecureStorage
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.module

// MIGRATION: iOS-specific Koin module providing platform implementations.
// IosSecureStorage and IosDataStore take no constructor arguments (they use
// iOS Keychain and NSUserDefaults respectively, which are process-global).

actual val platformModule: Module = module {
    single { SecureStorage() }
    single { AppDataStore() }
    single {
        EncryptionService().apply {
            runBlocking { initialize() }
        }
    }
}
