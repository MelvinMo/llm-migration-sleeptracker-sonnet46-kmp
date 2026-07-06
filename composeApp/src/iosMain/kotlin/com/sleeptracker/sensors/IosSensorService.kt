package com.sleeptracker.sensors

import com.sleeptracker.model.AccelerometerSensorData
import com.sleeptracker.model.AmbientNoiseLevel
import com.sleeptracker.model.AudioSensorData
import com.sleeptracker.model.FrequencyBands
import com.sleeptracker.model.LightLevel
import com.sleeptracker.model.LightSensorData
import com.sleeptracker.model.MovementIntensity
import com.benasher44.uuid.uuid4
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import platform.CoreMotion.CMMotionManager
import platform.Foundation.NSOperationQueue
import kotlin.math.sqrt

// MIGRATION: iOS sensor implementations
//
// LIGHT SENSOR:
//   TypeScript noted: "The ambient light sensor WILL NOT WORK through Expo Sensors on iOS devices."
//   Per the hard rules: "Light sensor on iOS: stub gracefully with a SensorNotAvailableWidget."
//   → isLightAvailable() returns false; lightDataFlow() returns emptyFlow()
//   → UI must show SensorNotAvailableWidget when running on iOS
//
// ACCELEROMETER:
//   iOS CoreMotion CMMotionManager replaces expo-sensors Accelerometer
//   TypeScript: `Accelerometer.addListener({ x, y, z })`
//   Kotlin:    `CMMotionManager.startAccelerometerUpdates(queue:) { data, error -> }`
//
// AUDIO:
//   iOS AVFoundation (AVAudioEngine) replaces expo-av Audio.Recording
//   MIGRATION_FLAG: Full AVAudioEngine integration requires Swift bridging or ObjC interop.
//   A simplified implementation is provided; production use requires AVFoundation permissions.

actual class PlatformSensorService {
    private val motionManager = CMMotionManager()

    // ─── Availability ─────────────────────────────────────────────────────────
    actual suspend fun isAudioAvailable(): Boolean = true  // AVFoundation available on all iOS

    // MIGRATION: Light sensor NOT available on iOS — stub returns false
    actual suspend fun isLightAvailable(): Boolean = false

    actual suspend fun isAccelerometerAvailable(): Boolean = motionManager.accelerometerAvailable

    // ─── Light sensor stub ────────────────────────────────────────────────────
    // MIGRATION: Per spec: "Light sensor on iOS: stub gracefully with a SensorNotAvailableWidget."
    // The UI layer checks isLightAvailable() and renders SensorNotAvailableWidget when false.
    actual suspend fun startLightMonitoring(intervalMs: Long) {
        // No-op on iOS — light sensor not accessible via public API
    }
    actual suspend fun stopLightMonitoring() { /* no-op */ }

    actual fun lightDataFlow(): Flow<LightSensorData> = emptyFlow()
    // ^ MIGRATION: emptyFlow() means no data arrives; UI shows SensorNotAvailableWidget

    // ─── Accelerometer ──────────────────────────────��────────────────────────���
    // MIGRATION: `Accelerometer.addListener({ x, y, z })` → CMMotionManager
    actual suspend fun startAccelerometerMonitoring(intervalMs: Long) {
        if (!motionManager.accelerometerAvailable) return
        motionManager.accelerometerUpdateInterval = intervalMs.toDouble() / 1000.0
    }
    actual suspend fun stopAccelerometerMonitoring() {
        motionManager.stopAccelerometerUpdates()
    }

    actual fun accelerometerDataFlow(): Flow<AccelerometerSensorData> = callbackFlow {
        if (!motionManager.accelerometerAvailable) {
            close()
            return@callbackFlow
        }
        motionManager.startAccelerometerUpdatesToQueue(NSOperationQueue.mainQueue) { data, error ->
            if (data != null) {
                val x = data.acceleration.x
                val y = data.acceleration.y
                val z = data.acceleration.z
                val magnitude = sqrt(x * x + y * y + z * z)
                trySend(
                    AccelerometerSensorData(
                        id = uuid4().toString(),
                        userId = "",
                        timestamp = Clock.System.now().toEpochMilliseconds().toString(),
                        date = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                        x = x.toString(),
                        y = y.toString(),
                        z = z.toString(),
                        magnitude = magnitude.toString(),
                        movementIntensity = categorizeMovement(magnitude)
                    )
                )
            }
        }
        awaitClose { motionManager.stopAccelerometerUpdates() }
    }.flowOn(Dispatchers.Default)

    // ─── Audio ──────────────────────��─────────────────────────────────────────
    // MIGRATION_FLAG: Full AVAudioEngine integration requires additional Kotlin/Native interop.
    // This provides simulation data as a fallback on iOS during development.
    // Production implementation should use AVAudioEngine via a Swift bridging header.
    actual suspend fun startAudioMonitoring() { /* AVAudioEngine setup */ }
    actual suspend fun stopAudioMonitoring() { /* AVAudioEngine teardown */ }

    actual fun audioDataFlow(): Flow<AudioSensorData> = callbackFlow {
        // MIGRATION_FLAG: Replace with real AVAudioEngine implementation for production.
        // For now, generate simulation data so the transparency UI has something to show.
        var running = true
        kotlinx.coroutines.launch {
            while (running) {
                val mockDbs = (30 + Math.random() * 40)
                trySend(
                    AudioSensorData(
                        id = uuid4().toString(),
                        userId = "",
                        timestamp = Clock.System.now().toEpochMilliseconds().toString(),
                        date = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                        averageDecibels = mockDbs.toString(),
                        peakDecibels = (mockDbs + Math.random() * 20).toString(),
                        frequencyBands = FrequencyBands(
                            low  = (Math.random() * 40).toString(),
                            mid  = (Math.random() * 50).toString(),
                            high = (Math.random() * 30).toString()
                        ),
                        snoreDetected = Math.random() > 0.9,
                        ambientNoiseLevel = categorizeNoiseLevel(mockDbs)
                    )
                )
                kotlinx.coroutines.delay(SensorSamplingRates.AUDIO_SECONDS * 1000)
            }
        }
        awaitClose { running = false }
    }.flowOn(Dispatchers.Default)

    private fun categorizeMovement(magnitude: Double): MovementIntensity = when {
        magnitude < 0.1 -> MovementIntensity.STILL
        magnitude < 0.5 -> MovementIntensity.LIGHT
        magnitude < 1.0 -> MovementIntensity.MODERATE
        else            -> MovementIntensity.ACTIVE
    }

    private fun categorizeNoiseLevel(dB: Double): AmbientNoiseLevel = when {
        dB < 30 -> AmbientNoiseLevel.QUIET
        dB < 50 -> AmbientNoiseLevel.MODERATE
        dB < 70 -> AmbientNoiseLevel.LOUD
        else    -> AmbientNoiseLevel.VERY_LOUD
    }
}
