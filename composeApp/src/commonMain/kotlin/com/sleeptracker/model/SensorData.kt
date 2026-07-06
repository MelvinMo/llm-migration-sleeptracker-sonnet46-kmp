package com.sleeptracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `interface BaseSensorReading` → abstract base data embedded in sealed class.
// TypeScript used structural typing with discriminated unions; Kotlin uses sealed classes for
// exhaustive `when` matching. All fields remain String to maintain SQLite TEXT column compatibility.

// MIGRATION: `type SensorData = AudioSensorData | LightSensorData | AccelerometerSensorData`
// → sealed class hierarchy. @Serializable on sealed class requires kotlinx.serialization 1.5+
// with the `classDiscriminator` annotation for polymorphic deserialization.
@Serializable
sealed class SensorData {
    abstract val id: String
    abstract val userId: String
    abstract val timestamp: String      // Unix ms as string (matches TypeScript `timestamp: string`)
    abstract val date: String           // YYYY-MM-DD
    abstract val sensorType: String     // discriminator value
}

// MIGRATION: `interface AudioSensorData extends BaseSensorReading`
// → data class extending sealed SensorData
@Serializable
@SerialName("audio")
data class AudioSensorData(
    override val id: String,
    override val userId: String,
    override val timestamp: String,
    override val date: String,
    override val sensorType: String = "audio",
    val averageDecibels: String,
    val peakDecibels: String,
    val frequencyBands: FrequencyBands,
    val audioClipUri: String? = null,
    val snoreDetected: Boolean,
    // MIGRATION: TypeScript union 'quiet' | 'moderate' | 'loud' | 'very_loud' → enum
    val ambientNoiseLevel: AmbientNoiseLevel
) : SensorData()

@Serializable
data class FrequencyBands(
    val low: String,    // 0–250 Hz
    val mid: String,    // 250–4000 Hz
    val high: String    // 4000+ Hz
)

@Serializable
enum class AmbientNoiseLevel {
    @SerialName("quiet") QUIET,
    @SerialName("moderate") MODERATE,
    @SerialName("loud") LOUD,
    @SerialName("very_loud") VERY_LOUD
}

@Serializable
@SerialName("light")
data class LightSensorData(
    override val id: String,
    override val userId: String,
    override val timestamp: String,
    override val date: String,
    override val sensorType: String = "light",
    val illuminance: String,            // Lux value as string for encryption compatibility
    val lightLevel: LightLevel
) : SensorData()

@Serializable
enum class LightLevel {
    @SerialName("dark") DARK,
    @SerialName("dim") DIM,
    @SerialName("moderate") MODERATE,
    @SerialName("bright") BRIGHT
}

@Serializable
@SerialName("accelerometer")
data class AccelerometerSensorData(
    override val id: String,
    override val userId: String,
    override val timestamp: String,
    override val date: String,
    override val sensorType: String = "accelerometer",
    val x: String,
    val y: String,
    val z: String,
    val magnitude: String,
    val movementIntensity: MovementIntensity
) : SensorData()

@Serializable
enum class MovementIntensity {
    @SerialName("still") STILL,
    @SerialName("light") LIGHT,
    @SerialName("moderate") MODERATE,
    @SerialName("active") ACTIVE
}
