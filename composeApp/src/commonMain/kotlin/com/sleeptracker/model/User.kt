package com.sleeptracker.model

import kotlinx.serialization.Serializable

// MIGRATION: TypeScript `type User` → @Serializable data class.
// No field is nullable because the source type has no optional fields.
@Serializable
data class User(
    val userId: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    // MIGRATION: `password` field kept for completeness but backend handles bcrypt hashing.
    // Client-side password is sent in registration only, never stored locally.
    val password: String = ""
)
