package com.fahim.geminiApiComposeStarter.ui.todo.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fahim.geminiApiComposeStarter.data.model.Priority
import com.fahim.geminiApiComposeStarter.ui.todo.TaskDialogState

@Composable
fun TaskDialog(
    dialogState: TaskDialogState,
    onDismiss: () -> Unit,
    onConfirm: (taskId: String?, description: String, priority: Priority) -> Unit
) {
    val isEdit = dialogState is TaskDialogState.Edit
    val initialTask = (dialogState as? TaskDialogState.Edit)?.task

    var description by remember(dialogState) {
        mutableStateOf(initialTask?.description.orEmpty())
    }
    var priority by remember(dialogState) {
        mutableStateOf(initialTask?.priority ?: Priority.MEDIUM)
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isEdit) Icons.Default.Edit else Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (isEdit) "Edit Task" else "Add New Task",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        description = it
                        if (it.isNotBlank()) isError = false
                    },
                    label = { Text("Task description") },
                    placeholder = { Text("e.g., Submit assignment by Friday") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text(
                                text = "Description cannot be empty",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Priority Level",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { level ->
                        val selected = level == priority
                        FilterChip(
                            selected = selected,
                            onClick = { priority = level },
                            label = {
                                Text(
                                    text = level.displayName,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = when (level) {
                                    Priority.HIGH -> MaterialTheme.colorScheme.errorContainer
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.tertiaryContainer
                                    Priority.LOW -> MaterialTheme.colorScheme.secondaryContainer
                                },
                                selectedLabelColor = when (level) {
                                    Priority.HIGH -> MaterialTheme.colorScheme.onErrorContainer
                                    Priority.MEDIUM -> MaterialTheme.colorScheme.onTertiaryContainer
                                    Priority.LOW -> MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (description.trim().isBlank()) {
                        isError = true
                    } else {
                        onConfirm(initialTask?.id, description, priority)
                    }
                }
            ) {
                Text(if (isEdit) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
