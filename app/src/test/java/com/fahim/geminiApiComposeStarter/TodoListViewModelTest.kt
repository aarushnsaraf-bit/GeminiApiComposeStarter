package com.fahim.geminiApiComposeStarter

import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import com.fahim.geminiApiComposeStarter.data.repository.InMemoryTaskRepository
import com.fahim.geminiApiComposeStarter.ui.todo.TaskDialogState
import com.fahim.geminiApiComposeStarter.ui.todo.TodoListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: InMemoryTaskRepository
    private lateinit var viewModel: TodoListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = InMemoryTaskRepository()
        viewModel = TodoListViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateStartsLoadingAndTransitionsToEmpty() = runTest(testDispatcher) {
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.tasks.isEmpty())
        assertTrue(state.isEmpty)
        assertNull(state.errorMessage)
    }

    @Test
    fun testAddTaskIncreasesCountAndSortsCorrectly() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.addTask("Low priority task", Priority.LOW)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.tasks.size)
        assertEquals(Priority.LOW, viewModel.uiState.value.tasks[0].priority)

        viewModel.addTask("High priority task", Priority.HIGH)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.tasks.size)

        // HIGH must be first
        assertEquals(Priority.HIGH, viewModel.uiState.value.tasks[0].priority)
        assertEquals("High priority task", viewModel.uiState.value.tasks[0].description)
        assertEquals(Priority.LOW, viewModel.uiState.value.tasks[1].priority)

        // Add MEDIUM priority task
        viewModel.addTask("Medium priority task", Priority.MEDIUM)
        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.tasks.size)

        // Order: HIGH -> MEDIUM -> LOW
        assertEquals(Priority.HIGH, viewModel.uiState.value.tasks[0].priority)
        assertEquals(Priority.MEDIUM, viewModel.uiState.value.tasks[1].priority)
        assertEquals(Priority.LOW, viewModel.uiState.value.tasks[2].priority)
    }

    @Test
    fun testUpdateTaskModifiesDescriptionAndPriority() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.addTask("Original Task", Priority.LOW)
        advanceUntilIdle()

        val createdTask = viewModel.uiState.value.tasks.first()
        viewModel.updateTask(createdTask.id, "Updated High Task", Priority.HIGH)
        advanceUntilIdle()

        val updated = viewModel.uiState.value.tasks.first()
        assertEquals("Updated High Task", updated.description)
        assertEquals(Priority.HIGH, updated.priority)
    }

    @Test
    fun testDeleteTaskRemovesTask() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.addTask("Task to delete", Priority.MEDIUM)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.tasks.size)

        val taskToDelete = viewModel.uiState.value.tasks.first()
        viewModel.deleteTask(taskToDelete.id)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.tasks.isEmpty())
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun testDialogStateTransitions() = runTest(testDispatcher) {
        assertNull(viewModel.uiState.value.activeDialog)

        viewModel.openAddDialog()
        assertEquals(TaskDialogState.Add, viewModel.uiState.value.activeDialog)

        viewModel.dismissDialog()
        assertNull(viewModel.uiState.value.activeDialog)

        val dummyTask = Task(id = "123", description = "Edit Me", priority = Priority.HIGH)
        viewModel.openEditDialog(dummyTask)
        assertTrue(viewModel.uiState.value.activeDialog is TaskDialogState.Edit)
        val editState = viewModel.uiState.value.activeDialog as TaskDialogState.Edit
        assertEquals("Edit Me", editState.task.description)
    }

    @Test
    fun testBlankDescriptionValidation() = runTest(testDispatcher) {
        advanceUntilIdle()

        viewModel.addTask("   ", Priority.HIGH)
        advanceUntilIdle()

        // Should not add empty task
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
        assertNotNull(viewModel.uiState.value.userMessage)
        assertEquals("Description cannot be empty", viewModel.uiState.value.userMessage)
    }
}
