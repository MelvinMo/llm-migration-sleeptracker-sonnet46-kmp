package com.sleeptracker.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `type JournalData` → @Serializable data class.
// All fields kept as String to match the SQLite TEXT columns exactly.
// `sleepNotes: SleepNote[]` → `List<SleepNote>` (Kotlin's generic List)
@Serializable
data class JournalData(
    val date: String,           // ISO date string e.g. "2025-04-03"
    val userId: String,
    val journalId: String,
    val bedtime: String,
    val alarmTime: String,
    val sleepDuration: String,
    val diaryEntry: String,
    val sleepNotes: List<SleepNote>
)

// MIGRATION: TypeScript union type `SleepNote = "Pain" | "Stress" | ...`
// → Kotlin sealed enum class. @SerialName preserves exact string values for SQLite JSON serialization.
// SQLite stores this as a JSON array (e.g. `["Pain","Stress"]`) in the sleepNotes TEXT column.
@Serializable
enum class SleepNote {
    @SerialName("Pain") Pain,
    @SerialName("Stress") Stress,
    @SerialName("Anxiety") Anxiety,
    @SerialName("Medication") Medication,
    @SerialName("Caffeine") Caffeine,
    @SerialName("Alcohol") Alcohol,
    @SerialName("Warm Bath") WarmBath,
    @SerialName("Heavy Meal") HeavyMeal
}
