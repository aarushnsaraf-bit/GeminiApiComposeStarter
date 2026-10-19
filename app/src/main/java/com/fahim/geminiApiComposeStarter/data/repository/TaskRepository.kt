package com.fahim.geminiApiComposeStarter.data.repository

import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for To-Do task management.
 * Abstracts the data source (Firebase Realtime Database or in-memory fallback).
 */
interface TaskRepository {
    /**
     * Observes all tasks in real time, ordered by priority (High -> Medium -> Low).
     */
    fun observeTasks(): Flow<Result<List<Task>>>

    /**
     * Adds a new task with [description] and [priority].
     * Returns the generated task ID on success.
     */
    suspend fun addTask(description: String, priority: Priority): Result<String>

    /**
     * Updates an existing task's description and priority.
     */
    suspend fun updateTask(taskId: String, description: String, priority: Priority): Result<Unit>

    /**
     * Deletes a task by its [taskId].
     */
    suspend fun deleteTask(taskId: String): Result<Unit>

    /**
     * Checks if the backing remote database is properly initialized and configured.
     */
    fun isConfigured(): Boolean
}
