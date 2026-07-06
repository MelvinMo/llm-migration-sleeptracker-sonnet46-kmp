package com.sleeptracker.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
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
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.sqrt
import kotlin.math.log10

// MIGRATION: TypeScript `ExpoSensorService.ts` used Expo APIs:
//   - `LightSensor` (expo-sensors) → Android `SensorManager.TYPE_LIGHT`
//   - `Accelerometer` (expo-sensors) → Android `SensorManager.TYPE_ACCELEROMETER`
//   - `Audio.Recording` (expo-av) → Android `AudioRecord`
//
// Flow<T> replaces the Expo listener/subscription + callback pattern.
// `callbackFlow` bridges the imperative SensorEventListener API to reactive Flows.

actual class PlatformSensorService(private val context: Context) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    // ─── Availability checks ──────────────────────────────────────────────────
    actual suspend fun isAudioAvailable(): Boolean = try {
        // MIGRATION: `Audio.requestPermissionsAsync()` → runtime permission check
        // Permission must be granted via the Compose permission launcher before calling this
        AudioRecord.ERROR != AudioRecord.getMinBufferSize(
            44100, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
    } catch (_: Exception) { false }

    actual suspend fun isLightAvailable(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null

    actual suspend fun isAccelerometerAvailable(): Boolean =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null

    // ─── Light sensor ─────────────────────────────────────────────────────────
    // MIGRATION: `LightSensor.addListener({ illuminance }) => {...}`
    // → callbackFlow wrapping SensorEventListener
    private var lightListener: SensorEventListener? = null

    actual suspend fun startLightMonitoring(intervalMs: Long) { /* managed via lightDataFlow() */ }
    actual suspend fun stopLightMonitoring() {
        lightListener?.let { sensorManager.unregisterListener(it) }
        lightListener = null
    }

    actual fun lightDataFlow(): Flow<LightSensorData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
            ?: run { close(); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val lux = event.values[0]
                trySend(
                    LightSensorData(
                        id = uuid4().toString(),
                        userId = "",   // userId is injected at repository layer
                        timestamp = Clock.System.now().toEpochMilliseconds().toString(),
                        date = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                        illuminance = lux.toString(),
                        lightLevel = categorizeLightLevel(lux)
                    )
                )
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        lightListener = listener
        // MIGRATION: `LightSensor.setUpdateInterval(intervalMs)` → SENSOR_DELAY_NORMAL or custom
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.flowOn(Dispatchers.Default)

    // ─── Accelerometer ────────────────────────────────────────────────────────
    private var accelListener: SensorEventListener? = null

    actual suspend fun startAccelerometerMonitoring(intervalMs: Long) { /* managed via flow */ }
    actual suspend fun stopAccelerometerMonitoring() {
        accelListener?.let { sensorManager.unregisterListener(it) }
        accelListener = null
    }

    // MIGRATION: `Accelerometer.addListener({ x, y, z }) => {...}` → callbackFlow
    actual fun accelerometerDataFlow(): Flow<AccelerometerSensorData> = callbackFlow {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: run { close(); return@callbackFlow }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0].toDouble()
                val y = event.values[1].toDouble()
                val z = event.values[2].toDouble()
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
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        accelListener = listener
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        awaitClose { sensorManager.unregisterListener(listener) }
    }.flowOn(Dispatchers.Default)

    // ─── Audio (microphone) ───────────────────────────────────────────────────
    // MIGRATION: `Audio.Recording.createAsync(HIGH_QUALITY)` + periodic `analyzeAudioData()`
    // → Android `AudioRecord` with periodic RMS dB computation on raw PCM data
    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    actual suspend fun startAudioMonitoring() {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
        audioRecord?.startRecording()
        isRecording = true
    }

    actual suspend fun stopAudioMonitoring() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }

    // MIGRATION: `analyzeAudioData()` with mock decibels in TypeScript → real RMS dB from PCM
    actual fun audioDataFlow(): Flow<AudioSensorData> = callbackFlow {
        val sampleRate = 44100
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT
        )
        val record = AudioRecord(
            MediaRecorder.AudioSource.MIC, sampleRate,
            AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize
        )
        record.startRecording()
        val buffer = ShortArray(bufferSize)
        try {
            while (true) {
                val read = record.read(buffer, 0, bufferSize)
                if (read > 0) {
                    val rms = sqrt(buffer.take(read).map { it.toLong() * it }.sum().toDouble() / read)
                    val dbValue = if (rms > 0) 20 * log10(rms) else 0.0
                    val peakDb = buffer.take(read).maxOf { it.toInt().let { v -> if (v < 0) -v else v } }
                        .toDouble().let { if (it > 0) 20 * log10(it) else 0.0 }

                    trySend(
                        AudioSensorData(
                            id = uuid4().toString(),
                            userId = "",
                            timestamp = Clock.System.now().toEpochMilliseconds().toString(),
                            date = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString(),
                            averageDecibels = dbValue.toString(),
                            peakDecibels = peakDb.toString(),
                            frequencyBands = FrequencyBands(
                                low  = (dbValue * 0.4).toString(),
                                mid  = (dbValue * 0.45).toString(),
                                high = (dbValue * 0.15).toString()
                            ),
                            snoreDetected = dbValue > 60,   // basic snore threshold
                            ambientNoiseLevel = categorizeNoiseLevel(dbValue)
                        )
                    )
                }
                kotlinx.coroutines.delay(SensorSamplingRates.AUDIO_SECONDS * 1000)
            }
        } finally {
            record.stop()
            record.release()
        }
        awaitClose { record.stop(); record.release() }
    }.flowOn(Dispatchers.Default)

    // ─── Categorization helpers (same logic as TypeScript) ───────────────────
    private fun categorizeLightLevel(lux: Float): LightLevel = when {
        lux < 1f   -> LightLevel.DARK
        lux < 10f  -> LightLevel.DIM
        lux < 100f -> LightLevel.MODERATE
        else       -> LightLevel.BRIGHT
    }

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
