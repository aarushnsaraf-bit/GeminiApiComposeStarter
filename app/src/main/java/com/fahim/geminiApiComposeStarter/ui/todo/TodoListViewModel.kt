package com.fahim.geminiApiComposeStarter.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import com.fahim.geminiApiComposeStarter.data.repository.InMemoryTaskRepository
import com.fahim.geminiApiComposeStarter.data.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing To-Do list state, reactive database updates, and task operations.
 */
class TodoListViewModel(
    private var repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        TodoListUiState(
            isLoading = true,
            isFirebaseConfigured = repository.isConfigured()
        )
    )
    val uiState: StateFlow<TodoListUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        startObservingTasks()
    }

    fun startObservingTasks() {
        observeJob?.cancel()
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        observeJob = viewModelScope.launch {
            repository.observeTasks().collect { result ->
                result.fold(
                    onSuccess = { tasks ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                tasks = tasks,
                                errorMessage = null
                            )
                        }
                    },
                    onFailure = { error ->
                        val isConfigError = !repository.isConfigured()
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.localizedMessage ?: "Failed to load tasks",
                                isFirebaseConfigured = !isConfigError
                            )
                        }
                    }
                )
            }
        }
    }

    fun openAddDialog() {
        _uiState.update { it.copy(activeDialog = TaskDialogState.Add) }
    }

    fun openEditDialog(task: Task) {
        _uiState.update { it.copy(activeDialog = TaskDialogState.Edit(task)) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(activeDialog = null) }
    }

    fun addTask(description: String, priority: Priority) {
        val trimmed = description.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(userMessage = "Description cannot be empty") }
            return
        }

        viewModelScope.launch {
            val result = repository.addTask(trimmed, priority)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            activeDialog = null,
                            userMessage = "Task added successfully"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            userMessage = "Error adding task: ${error.localizedMessage ?: error.message}"
                        )
                    }
                }
            )
        }
    }

    fun updateTask(taskId: String, description: String, priority: Priority) {
        val trimmed = description.trim()
        if (trimmed.isBlank()) {
            _uiState.update { it.copy(userMessage = "Description cannot be empty") }
            return
        }

        viewModelScope.launch {
            val result = repository.updateTask(taskId, trimmed, priority)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            activeDialog = null,
                            userMessage = "Task updated successfully"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            userMessage = "Error updating task: ${error.localizedMessage ?: error.message}"
                        )
                    }
                }
            )
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            val result = repository.deleteTask(taskId)
            result.fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(userMessage = "Task deleted")
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            userMessage = "Error deleting task: ${error.localizedMessage ?: error.message}"
                        )
                    }
                }
            )
        }
    }

    fun switchToDemoMode() {
        val sampleTasks = listOf(
            Task(
                id = "sample-1",
                description = "Submit Mobile App Dev assignment to portal",
                priority = Priority.HIGH,
                timestamp = System.currentTimeMillis() - 1000
            ),
            Task(
                id = "sample-2",
                description = "Prepare Viva questions on Firebase synchronization",
                priority = Priority.MEDIUM,
                timestamp = System.currentTimeMillis() - 2000
            ),
            Task(
                id = "sample-3",
                description = "Read documentation on Jetpack Compose SwipeToDismissBox",
                priority = Priority.LOW,
                timestamp = System.currentTimeMillis() - 3000
            )
        )
        repository = InMemoryTaskRepository(sampleTasks)
        _uiState.update {
            it.copy(
                isDemoMode = true,
                isFirebaseConfigured = true,
                errorMessage = null,
                userMessage = "Switched to Demo Mode (In-Memory)"
            )
        }
        startObservingTasks()
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    fun retry() {
        startObservingTasks()
    }

    companion object {
        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TodoListViewModel(repository) as T
                }
            }
    }
}
