package com.fahim.geminiApiComposeStarter.data.repository

import android.content.Context
import android.util.Log
import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.data.model.Task
import com.fahim.geminiApiComposeStarter.data.model.TaskDto
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Implementation of [TaskRepository] using Firebase Realtime Database.
 * Observes data changes reactively via [callbackFlow] and [ValueEventListener],
 * and sorts tasks High -> Medium -> Low dynamically.
 */
class FirebaseTaskRepository(
    private val context: Context,
    private val customDatabaseUrl: String? = null
) : TaskRepository {

    private val tag = "FirebaseTaskRepo"

    init {
        // Enable offline persistence if Firebase is configured
        if (isConfigured()) {
            try {
                getDatabase()?.setPersistenceEnabled(true)
            } catch (e: Exception) {
                // Persistence can only be configured once before database reference access
                Log.d(tag, "Persistence already configured or failed: ${e.message}")
            }
        }
    }

    override fun isConfigured(): Boolean {
        return try {
            FirebaseApp.getApps(context).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    private fun getDatabase(): FirebaseDatabase? {
        if (!isConfigured()) return null

        return try {
            FirebaseDatabase.getInstance(
                "https://mad-602ae-default-rtdb.asia-southeast1.firebasedatabase.app"
            )
        } catch (e: Exception) {
            Log.e(tag, "Error accessing FirebaseDatabase instance", e)
            null
        }
    }

    private fun getTasksReference(): DatabaseReference? {
        return getDatabase()?.getReference("tasks")
    }

    override fun observeTasks(): Flow<Result<List<Task>>> = callbackFlow {
        val tasksRef = getTasksReference()
        if (tasksRef == null) {
            trySend(
                Result.failure(
                    IllegalStateException(
                        "Firebase is not initialized. Please configure your Firebase project and add google-services.json to the app/ directory."
                    )
                )
            )
            close()
            return@callbackFlow
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    val taskList = mutableListOf<Task>()
                    for (child in snapshot.children) {
                        val key = child.key ?: continue
                        val dto = child.getValue(TaskDto::class.java)
                        val task = dto?.toTask(fallbackId = key) ?: Task(
                            id = key,
                            description = child.child("description").getValue(String::class.java).orEmpty(),
                            priority = Priority.fromString(child.child("priority").getValue(String::class.java)),
                            timestamp = child.child("timestamp").getValue(Long::class.java) ?: 0L,
                            isCompleted = child.child("completed").getValue(Boolean::class.java) ?: false
                        )
                        taskList.add(task)
                    }

                    // Sort tasks High -> Medium -> Low, then by newest timestamp descending
                    val sortedList = taskList.sortedWith(
                        compareByDescending<Task> { it.priority.level }
                            .thenByDescending { it.timestamp }
                    )

                    trySend(Result.success(sortedList))
                } catch (e: Exception) {
                    Log.e(tag, "Error parsing tasks snapshot", e)
                    trySend(Result.failure(e))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(tag, "Firebase Database error: ${error.message}", error.toException())
                trySend(Result.failure(error.toException()))
            }
        }

        tasksRef.addValueEventListener(listener)

        awaitClose {
            tasksRef.removeEventListener(listener)
        }
    }

    override suspend fun addTask(description: String, priority: Priority): Result<String> {
        val tasksRef = getTasksReference()
            ?: return Result.failure(
                IllegalStateException("Firebase is not initialized. Please add google-services.json.")
            )

        return try {
            val newRef = tasksRef.push()
            val taskId = newRef.key ?: return Result.failure(IllegalStateException("Failed to generate task ID"))
            val task = Task(
                id = taskId,
                description = description.trim(),
                priority = priority,
                timestamp = System.currentTimeMillis(),
                isCompleted = false
            )
            newRef.setValue(task.toMap()).await()
            Result.success(taskId)
        } catch (e: Exception) {
            Log.e(tag, "Error adding task to Firebase", e)
            Result.failure(e)
        }
    }

    override suspend fun updateTask(
        taskId: String,
        description: String,
        priority: Priority
    ): Result<Unit> {
        val tasksRef = getTasksReference()
            ?: return Result.failure(
                IllegalStateException("Firebase is not initialized. Please add google-services.json.")
            )

        return try {
            val updates = mapOf<String, Any>(
                "description" to description.trim(),
                "priority" to priority.name,
                "timestamp" to System.currentTimeMillis()
            )
            tasksRef.child(taskId).updateChildren(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error updating task in Firebase", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(taskId: String): Result<Unit> {
        val tasksRef = getTasksReference()
            ?: return Result.failure(
                IllegalStateException("Firebase is not initialized. Please add google-services.json.")
            )

        return try {
            tasksRef.child(taskId).removeValue().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error deleting task from Firebase", e)
            Result.failure(e)
        }
    }
}
