package com.fahim.geminiApiComposeStarter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.fahim.geminiApiComposeStarter.data.repository.FirebaseTaskRepository
import com.fahim.geminiApiComposeStarter.ui.theme.GeminiApiComposeStarterTheme
import com.fahim.geminiApiComposeStarter.ui.todo.TodoListScreen
import com.fahim.geminiApiComposeStarter.ui.todo.TodoListViewModel

class MainActivity : ComponentActivity() {

    private val todoViewModel: TodoListViewModel by viewModels {
        val repository = FirebaseTaskRepository(applicationContext)
        TodoListViewModel.factory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeminiApiComposeStarterTheme {
                TodoListScreen(viewModel = todoViewModel)
            }
        }
    }
}
