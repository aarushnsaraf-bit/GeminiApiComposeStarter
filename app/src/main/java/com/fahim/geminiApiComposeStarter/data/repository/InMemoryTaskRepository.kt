package com.fahim.geminiApiComposeStarter.data.repository

import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * In-memory implementation of [TaskRepository].
 * Used for testing, preview, and standalone demo when Firebase credentials are not yet configured.
 */
class InMemoryTaskRepository(initialTasks: List<Task> = emptyList()) : TaskRepository {

    private val _tasks = MutableStateFlow(initialTasks)

    override fun observeTasks(): Flow<Result<List<Task>>> {
        return _tasks.map { list ->
            // Sort High -> Medium -> Low, then newest timestamp descending
            val sorted = list.sortedWith(
                compareByDescending<Task> { it.priority.level }
                    .thenByDescending { it.timestamp }
            )
            Result.success(sorted)
        }
    }

    override suspend fun addTask(description: String, priority: Priority): Result<String> {
        val id = UUID.randomUUID().toString()
        val newTask = Task(
            id = id,
            description = description.trim(),
            priority = priority,
            timestamp = System.currentTimeMillis(),
            isCompleted = false
        )
        _tasks.value = _tasks.value + newTask
        return Result.success(id)
    }

    override suspend fun updateTask(
        taskId: String,
        description: String,
        priority: Priority
    ): Result<Unit> {
        val current = _tasks.value
        val index = current.indexOfFirst { it.id == taskId }
        if (index == -1) {
            return Result.failure(NoSuchElementException("Task $taskId not found"))
        }
        val updatedTask = current[index].copy(
            description = description.trim(),
            priority = priority,
            timestamp = System.currentTimeMillis()
        )
        val mutable = current.toMutableList()
        mutable[index] = updatedTask
        _tasks.value = mutable
        return Result.success(Unit)
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        _tasks.value = _tasks.value.filterNot { it.id == taskId }
        return Result.success(Unit)
    }

    override fun isConfigured(): Boolean = true
}
