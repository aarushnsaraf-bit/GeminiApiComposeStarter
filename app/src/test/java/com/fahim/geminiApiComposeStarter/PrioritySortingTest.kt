package com.fahim.geminiApiComposeStarter

import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PrioritySortingTest {

    @Test
    fun testPriorityEnumLevels() {
        assertTrue(Priority.HIGH.level > Priority.MEDIUM.level)
        assertTrue(Priority.MEDIUM.level > Priority.LOW.level)
        assertEquals(3, Priority.HIGH.level)
        assertEquals(2, Priority.MEDIUM.level)
        assertEquals(1, Priority.LOW.level)
    }

    @Test
    fun testPriorityFromStringParsing() {
        assertEquals(Priority.HIGH, Priority.fromString("HIGH"))
        assertEquals(Priority.HIGH, Priority.fromString("high"))
        assertEquals(Priority.MEDIUM, Priority.fromString("Medium"))
        assertEquals(Priority.LOW, Priority.fromString("low"))
        // Fallback for null or invalid
        assertEquals(Priority.MEDIUM, Priority.fromString(null))
        assertEquals(Priority.MEDIUM, Priority.fromString("UNKNOWN_VALUE"))
    }

    @Test
    fun testTasksSortedHighToMediumToLow() {
        val lowTask = Task(id = "1", description = "Low Task", priority = Priority.LOW, timestamp = 1000L)
        val mediumTask = Task(id = "2", description = "Med Task", priority = Priority.MEDIUM, timestamp = 2000L)
        val highTask = Task(id = "3", description = "High Task", priority = Priority.HIGH, timestamp = 3000L)

        val unsorted = listOf(lowTask, highTask, mediumTask)

        val sorted = unsorted.sortedWith(
            compareByDescending<Task> { it.priority.level }
                .thenByDescending { it.timestamp }
        )

        assertEquals("High Task", sorted[0].description)
        assertEquals("Med Task", sorted[1].description)
        assertEquals("Low Task", sorted[2].description)
    }

    @Test
    fun testTasksSamePrioritySortedByNewestTimestamp() {
        val highOld = Task(id = "1", description = "High Old", priority = Priority.HIGH, timestamp = 1000L)
        val highNew = Task(id = "2", description = "High New", priority = Priority.HIGH, timestamp = 5000L)

        val sorted = listOf(highOld, highNew).sortedWith(
            compareByDescending<Task> { it.priority.level }
                .thenByDescending { it.timestamp }
        )

        assertEquals("High New", sorted[0].description)
        assertEquals("High Old", sorted[1].description)
    }
}
