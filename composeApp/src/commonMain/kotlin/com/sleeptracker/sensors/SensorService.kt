package com.sleeptracker.sensors

import com.sleeptracker.model.AccelerometerSensorData
import com.sleeptracker.model.AudioSensorData
import com.sleeptracker.model.LightSensorData
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.toLocalDateTime

// MIGRATION: TypeScript `SensorService.ts` was an abstract class with callbacks.
// In KMP we use Kotlin Flows instead of callbacks for reactive sensor data streams.
// Flow<T> is the idiomatic KMP pattern for reactive data — it handles backpressure and
// coroutine cancellation automatically, replacing setInterval + callback patterns.

// MIGRATION: `expect class` — platform implementations provide the actual sensor APIs:
//   Android: SensorManager (TYPE_LIGHT, TYPE_ACCELEROMETER) + AudioRecord
//   iOS:     CMMotionManager (accelerometer) + AVAudioEngine (audio) + STUB for light sensor
//            (iOS does not expose the ambient light sensor via public API)
expect class PlatformSensorService {
    // Availability checks (replaces isAudioAvailable(), isLightAvailable(), etc.)
    suspend fun isAudioAvailable(): Boolean
    suspend fun isLightAvailable(): Boolean      // Always false on iOS
    suspend fun isAccelerometerAvailable(): Boolean

    // Start/stop monitoring — return Flows instead of callbacks
    // MIGRATION: setInterval + callback pattern → Flow<T> with coroutine emission
    fun audioDataFlow(): Flow<AudioSensorData>
    fun lightDataFlow(): Flow<LightSensorData>
    fun accelerometerDataFlow(): Flow<AccelerometerSensorData>

    suspend fun startAudioMonitoring()
    suspend fun stopAudioMonitoring()
    suspend fun startLightMonitoring(intervalMs: Long)
    suspend fun stopLightMonitoring()
    suspend fun startAccelerometerMonitoring(intervalMs: Long)
    suspend fun stopAccelerometerMonitoring()
}

// ─── Sensor config ────────────────────────────────────────────────────────────
// MIGRATION: TypeScript `SensorServiceConfig` interface → data class
data class SensorServiceConfig(
    val accelerometerEnabled: Boolean = false,
    val lightSensorEnabled: Boolean = false,
    val microphoneEnabled: Boolean = false
)

// Default sampling rates (seconds) — matches TypeScript sensorConfig.ts
object SensorSamplingRates {
    const val AUDIO_SECONDS = 30L         // 30s analysis interval
    const val LIGHT_SECONDS = 5L          // 5s update interval
    const val ACCELEROMETER_SECONDS = 1L  // 1s update interval
}

// ─── Simulation sensor service ────────────────────────────────────────────────
// MIGRATION: TypeScript `SimulationSensorService.ts` → SimulationSensorService in commonMain.
// This is used when IN_DEMO_MODE=true, generating realistic mock data without real hardware.
// In common code because the simulation logic is platform-independent.
object SimulationSensorService {

    fun simulatedAudioData(userId: String): AudioSensorData {
        val mockDecibels = (30 + (Math.random() * 40)).toString()
        val mockPeak = ((mockDecibels.toDouble()) + (Math.random() * 20)).toString()
        return AudioSensorData(
            id = generateId(),
            userId = userId,
            timestamp = currentEpochMs().toString(),
            date = todayIsoDate(),
            averageDecibels = mockDecibels,
            peakDecibels = mockPeak,
            frequencyBands = com.sleeptracker.model.FrequencyBands(
                low  = (Math.random() * 40).toString(),
                mid  = (Math.random() * 50).toString(),
                high = (Math.random() * 30).toString()
            ),
            snoreDetected = Math.random() > 0.9,
            ambientNoiseLevel = categorizeNoiseLevel(mockDecibels.toDouble())
        )
    }

    fun simulatedLightData(userId: String): LightSensorData {
        val lux = Math.random() * 10  // typically dark during sleep
        return LightSensorData(
            id = generateId(),
            userId = userId,
            timestamp = currentEpochMs().toString(),
            date = todayIsoDate(),
            illuminance = lux.toString(),
            lightLevel = categorizeLightLevel(lux)
        )
    }

    fun simulatedAccelerometerData(userId: String): AccelerometerSensorData {
        val x = (Math.random() * 0.2 - 0.1)
        val y = (Math.random() * 0.2 - 0.1)
        val z = (9.8 + Math.random() * 0.1 - 0.05) // gravity dominant
        val magnitude = Math.sqrt(x * x + y * y + z * z)
        return AccelerometerSensorData(
            id = generateId(),
            userId = userId,
            timestamp = currentEpochMs().toString(),
            date = todayIsoDate(),
            x = x.toString(),
            y = y.toString(),
            z = z.toString(),
            magnitude = magnitude.toString(),
            movementIntensity = categorizeMovement(magnitude)
        )
    }

    private fun categorizeNoiseLevel(dB: Double) = when {
        dB < 30 -> com.sleeptracker.model.AmbientNoiseLevel.QUIET
        dB < 50 -> com.sleeptracker.model.AmbientNoiseLevel.MODERATE
        dB < 70 -> com.sleeptracker.model.AmbientNoiseLevel.LOUD
        else    -> com.sleeptracker.model.AmbientNoiseLevel.VERY_LOUD
    }

    private fun categorizeLightLevel(lux: Double) = when {
        lux < 1   -> com.sleeptracker.model.LightLevel.DARK
        lux < 10  -> com.sleeptracker.model.LightLevel.DIM
        lux < 100 -> com.sleeptracker.model.LightLevel.MODERATE
        else      -> com.sleeptracker.model.LightLevel.BRIGHT
    }

    private fun categorizeMovement(magnitude: Double) = when {
        magnitude < 0.1 -> com.sleeptracker.model.MovementIntensity.STILL
        magnitude < 0.5 -> com.sleeptracker.model.MovementIntensity.LIGHT
        magnitude < 1.0 -> com.sleeptracker.model.MovementIntensity.MODERATE
        else            -> com.sleeptracker.model.MovementIntensity.ACTIVE
    }

    // expect declarations for platform-specific helpers
    private fun generateId(): String = com.benasher44.uuid.uuid4().toString()
    private fun currentEpochMs(): Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
    private fun todayIsoDate(): String {
        val now = kotlinx.datetime.Clock.System.now()
            .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
            .date
        return now.toString()
    }
}
