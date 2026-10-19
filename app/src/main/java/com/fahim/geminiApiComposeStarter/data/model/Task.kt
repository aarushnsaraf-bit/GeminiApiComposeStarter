package com.fahim.geminiApiComposeStarter.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Domain model representing a To-Do Task.
 */
data class Task(
    val id: String = "",
    val description: String = "",
    val priority: Priority = Priority.MEDIUM,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false
) {
    /**
     * Converts domain model into a Firebase Realtime Database Map representation.
     */
    fun toMap(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "description" to description,
            "priority" to priority.name,
            "timestamp" to timestamp,
            "completed" to isCompleted
        )
    }
}

/**
 * Firebase Data Transfer Object (DTO) for Firebase Realtime Database.
 * Must have a no-argument constructor for Firebase deserialization.
 */
@IgnoreExtraProperties
data class TaskDto(
    var id: String? = null,
    var description: String? = null,
    var priority: String? = null,
    var timestamp: Long? = null,
    var completed: Boolean? = null
) {
    /**
     * Maps the DTO to a strongly typed domain [Task].
     */
    fun toTask(fallbackId: String = ""): Task {
        return Task(
            id = id?.takeIf { it.isNotBlank() } ?: fallbackId,
            description = description.orEmpty(),
            priority = Priority.fromString(priority),
            timestamp = timestamp ?: System.currentTimeMillis(),
            isCompleted = completed ?: false
        )
    }
}
