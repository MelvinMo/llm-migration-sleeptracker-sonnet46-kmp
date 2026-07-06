package com.sleeptracker

import android.app.Application
import com.sleeptracker.di.dataModule
import com.sleeptracker.di.networkModule
import com.sleeptracker.di.platformModule
import com.sleeptracker.di.viewModelModule
import com.sleeptracker.resources.ResourceLoader
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

// MIGRATION: Koin moved from MainActivity.onCreate() → Application.onCreate().
// This is the correct lifecycle point: the Application is created once per process,
// so Koin is guaranteed to start before any Activity, Service, or BroadcastReceiver.
// ResourceLoader.load() must complete before startKoin so that
// ResourceLoader.privacyPolicyJson is ready when networkModule creates TransparencyApiService.

class SleepTrackerApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Make applicationContext available to SqlDriver and other platform utils
        com.sleeptracker.data.local.applicationContext = this.applicationContext

        // Load JSON resources from composeResources synchronously before Koin starts.
        // These are small files (~30 KB total); blocking here is acceptable.
        runBlocking {
            ResourceLoader.load()
        }

        startKoin {
            androidContext(this@SleepTrackerApplication)
            modules(platformModule, networkModule, dataModule, viewModelModule)
        }
    }
}
