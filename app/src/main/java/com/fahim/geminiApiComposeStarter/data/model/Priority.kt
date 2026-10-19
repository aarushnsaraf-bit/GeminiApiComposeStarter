package com.fahim.geminiApiComposeStarter.data.model

import androidx.compose.ui.graphics.Color

/**
 * Represents the priority level of a To-Do task.
 * [level] is used to sort tasks High (3) -> Medium (2) -> Low (1).
 */
enum class Priority(val level: Int, val displayName: String) {
    HIGH(3, "High"),
    MEDIUM(2, "Medium"),
    LOW(1, "Low");

    companion object {
        /**
         * Safely parse a Priority from a string, defaulting to [MEDIUM].
         */
        fun fromString(value: String?): Priority {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) }
                ?: entries.firstOrNull { it.displayName.equals(value, ignoreCase = true) }
                ?: MEDIUM
        }
    }
}
