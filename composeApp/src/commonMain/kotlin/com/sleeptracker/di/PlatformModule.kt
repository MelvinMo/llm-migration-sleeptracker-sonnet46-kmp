package com.sleeptracker.di

import org.koin.core.module.Module

// MIGRATION: Replaces platform-specific manual DI wiring in MainActivity.
// Each platform provides concrete implementations of expect classes:
//   Android → AndroidSecureStorage(context), AndroidDataStore(context)
//   iOS     → IosSecureStorage(), IosDataStore()
// The expect/actual pattern maps to separate Koin modules per platform.

expect val platformModule: Module
