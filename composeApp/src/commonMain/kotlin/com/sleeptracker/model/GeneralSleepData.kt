package com.sleeptracker.model

import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `type GeneralSleepData` → @Serializable data class.
// All fields are String matching the backend's Firestore string storage.
@Serializable
data class GeneralSleepData(
    val userId: String,
    val currentSleepDuration: String,
    val snoring: String,
    val tirednessFrequency: String,
    val daytimeSleepiness: String
)
