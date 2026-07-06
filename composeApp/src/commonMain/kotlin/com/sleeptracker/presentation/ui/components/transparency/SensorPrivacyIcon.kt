package com.sleeptracker.presentation.ui.components.transparency

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import sleeptracker.composeapp.generated.resources.Res
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_cloud_high
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_cloud_medium
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_cloud_low
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_local_high
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_local_medium
import sleeptracker.composeapp.generated.resources.sensor_accelerometer_local_low
import sleeptracker.composeapp.generated.resources.sensor_light_cloud_high
import sleeptracker.composeapp.generated.resources.sensor_light_cloud_medium
import sleeptracker.composeapp.generated.resources.sensor_light_cloud_low
import sleeptracker.composeapp.generated.resources.sensor_light_local_high
import sleeptracker.composeapp.generated.resources.sensor_light_local_medium
import sleeptracker.composeapp.generated.resources.sensor_light_local_low
import sleeptracker.composeapp.generated.resources.sensor_microphone_cloud_high
import sleeptracker.composeapp.generated.resources.sensor_microphone_cloud_medium
import sleeptracker.composeapp.generated.resources.sensor_microphone_cloud_low
import sleeptracker.composeapp.generated.resources.sensor_microphone_local_high
import sleeptracker.composeapp.generated.resources.sensor_microphone_local_medium
import sleeptracker.composeapp.generated.resources.sensor_microphone_local_low

@OptIn(ExperimentalResourceApi::class)
@Composable
fun SensorPrivacyIcon(
    sensorType: String,     // "accelerometer", "light", "microphone"
    iconName: String,       // "privacy_low" | "privacy_medium" | "privacy_high"
    storageType: String,    // "cloud" | "local"
    onPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val resource: DrawableResource = when (sensorType) {
        "accelerometer" -> when (storageType) {
            "cloud" -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_accelerometer_cloud_high
                "privacy_medium" -> Res.drawable.sensor_accelerometer_cloud_medium
                else             -> Res.drawable.sensor_accelerometer_cloud_low
            }
            else -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_accelerometer_local_high
                "privacy_medium" -> Res.drawable.sensor_accelerometer_local_medium
                else             -> Res.drawable.sensor_accelerometer_local_low
            }
        }
        "light" -> when (storageType) {
            "cloud" -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_light_cloud_high
                "privacy_medium" -> Res.drawable.sensor_light_cloud_medium
                else             -> Res.drawable.sensor_light_cloud_low
            }
            else -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_light_local_high
                "privacy_medium" -> Res.drawable.sensor_light_local_medium
                else             -> Res.drawable.sensor_light_local_low
            }
        }
        else -> when (storageType) { // microphone
            "cloud" -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_microphone_cloud_high
                "privacy_medium" -> Res.drawable.sensor_microphone_cloud_medium
                else             -> Res.drawable.sensor_microphone_cloud_low
            }
            else -> when (iconName) {
                "privacy_high"   -> Res.drawable.sensor_microphone_local_high
                "privacy_medium" -> Res.drawable.sensor_microphone_local_medium
                else             -> Res.drawable.sensor_microphone_local_low
            }
        }
    }

    Box(
        modifier         = modifier
            .size(48.dp)
            .clickable(onClick = onPress),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painterResource(resource),
            contentDescription = "Sensor: $sensorType, risk: $iconName",
            contentScale       = ContentScale.Fit,
            modifier           = Modifier.size(48.dp)
        )
    }
}
