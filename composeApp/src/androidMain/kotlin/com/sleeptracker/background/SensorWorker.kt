package com.sleeptracker.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.sleeptracker.sensors.PlatformSensorService
import com.sleeptracker.sensors.SensorSamplingRates
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

// MIGRATION: TypeScript `BackgroundTaskManager.ts` used `expo-background-fetch` and
// `expo-task-manager` for background sensor collection.
// Android 8+ requires a ForegroundService with a persistent notification for background work.
// WorkManager + CoroutineWorker is the idiomatic AndroidX replacement.
//
// MIGRATION: `expo-background-fetch` had a time limit of ~30s. WorkManager runs
// continuously as a long-running worker when started as a foreground worker.

const val CHANNEL_ID = "sleep_tracker_sensors"
const val NOTIFICATION_ID = 1001

class SensorWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun getForegroundInfo(): ForegroundInfo {
        createNotificationChannel()
        val notification = buildNotification()
        // MIGRATION: Android 8+ foreground service type required for microphone + motion access
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    override suspend fun doWork(): Result = coroutineScope {
        val audioEnabled = inputData.getBoolean("audioEnabled", false)
        val lightEnabled = inputData.getBoolean("lightEnabled", false)
        val accelEnabled = inputData.getBoolean("accelEnabled", false)

        setForeground(getForegroundInfo())

        val sensorService = PlatformSensorService(context)

        // Collect sensor streams concurrently
        // MIGRATION: TypeScript `setInterval` for each sensor → parallel Flow collection
        if (audioEnabled && sensorService.isAudioAvailable()) {
            sensorService.audioDataFlow()
                .onEach { data ->
                    // TODO: Persist to SQLDelight and update transparency events
                    // Would inject LocalDatabaseManager and TransparencyViewModel via DI in prod
                }
                .launchIn(this)
        }

        if (lightEnabled && sensorService.isLightAvailable()) {
            sensorService.lightDataFlow()
                .onEach { data ->
                    // TODO: Persist light data
                }
                .launchIn(this)
        }

        if (accelEnabled && sensorService.isAccelerometerAvailable()) {
            sensorService.accelerometerDataFlow()
                .onEach { data ->
                    // TODO: Persist accelerometer data
                }
                .launchIn(this)
        }

        // Worker runs until cancelled (when sleep session ends)
        Result.success()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracker Sensors",
                NotificationManager.IMPORTANCE_LOW   // Low: no sound, shown in status bar
            ).apply {
                description = "Monitors sleep environment during sleep sessions"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Sleep Tracker Active")
            .setContentText("Monitoring your sleep environment")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // MIGRATION_FLAG: Replace with app icon
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
}
