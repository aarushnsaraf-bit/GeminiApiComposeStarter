package com.fahim.geminiApiComposeStarter.ui.todo

import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task

/**
 * State of the Add/Edit task dialog.
 */
sealed interface TaskDialogState {
    data object Add : TaskDialogState
    data class Edit(val task: Task) : TaskDialogState
}

/**
 * UI State for the To-Do list screen.
 */
data class TodoListUiState(
    val isLoading: Boolean = true,
    val tasks: List<Task> = emptyList(),
    val errorMessage: String? = null,
    val isFirebaseConfigured: Boolean = true,
    val activeDialog: TaskDialogState? = null,
    val userMessage: String? = null,
    val isDemoMode: Boolean = false
) {
    val highCount: Int get() = tasks.count { it.priority == Priority.HIGH }
    val mediumCount: Int get() = tasks.count { it.priority == Priority.MEDIUM }
    val lowCount: Int get() = tasks.count { it.priority == Priority.LOW }
    val isEmpty: Boolean get() = !isLoading && tasks.isEmpty() && errorMessage == null
}
