package com.sleeptracker.background

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

// MIGRATION: TypeScript `BackgroundTaskManager.ts` used:
//   `expo-background-fetch` → WorkManager with setForeground()
//   `expo-task-manager` task registration → unique work named "SENSOR_TASK"
//
// `updateConfig({ audioEnabled, lightEnabled })` from sleep-mode.tsx is preserved.

const val SENSOR_WORK_NAME = "SENSOR_TASK" // Same task name as TypeScript for traceability

object BackgroundTaskManager {

    // MIGRATION: `registerAccelerometer()` from BackgroundTaskManager.ts
    // → enqueueUniqueWork with REPLACE policy so re-starting is idempotent
    fun startSensorCollection(
        context: Context,
        audioEnabled: Boolean,
        lightEnabled: Boolean,
        accelEnabled: Boolean
    ) {
        val inputData = Data.Builder()
            .putBoolean("audioEnabled", audioEnabled)
            .putBoolean("lightEnabled", lightEnabled)
            .putBoolean("accelEnabled", accelEnabled)
            .build()

        val request = OneTimeWorkRequestBuilder<SensorWorker>()
            .setInputData(inputData)
            .build()

        // MIGRATION: REPLACE ensures only one sensor worker runs at a time —
        // same as TypeScript's single background task registration
        WorkManager.getInstance(context).enqueueUniqueWork(
            SENSOR_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // MIGRATION: `updateConfig({ audioEnabled: false, lightEnabled: false })` from handleWakeUp()
    // → cancel the unique work, stopping all sensor collection
    fun stopSensorCollection(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(SENSOR_WORK_NAME)
    }
}
