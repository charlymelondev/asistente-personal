package com.carlos.asistente.ui.screens.inbox

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.asistente.ui.components.AnimatedAvatar
import com.carlos.asistente.ui.components.AvatarState
import com.carlos.asistente.ui.components.TaskCard
import com.carlos.asistente.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(
    viewModel: InboxViewModel = viewModel(),
    onTaskClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePickerForTaskId by remember { mutableStateOf<String?>(null) }

    // Refresh every time this screen enters composition
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Inbox,
                    contentDescription = null,
                    tint = CoralAccent,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Buzón de entrada",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "Tareas sin fecha asignada",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            if (state.tasks.isEmpty() && !state.isLoading) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        AnimatedAvatar(
                            state = AvatarState.CELEBRATING,
                            size = 120.dp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Buzón vacío!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Todas las tareas tienen fecha asignada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    itemsIndexed(
                        items = state.tasks,
                        key = { _, task -> task.id }
                    ) { index, task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(
                                animationSpec = tween(
                                    durationMillis = 300,
                                    delayMillis = index * 40
                                )
                            ) + slideInVertically(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy
                                ),
                                initialOffsetY = { it / 2 }
                            )
                        ) {
                            Column {
                                TaskCard(
                                    task = task,
                                    onDone = { viewModel.markDone(task.id) },
                                    onDelete = { viewModel.deleteTask(task.id) },
                                    onClick = { onTaskClick(task.id) }
                                )

                                // Assign date button below each card
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp, top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AssistChip(
                                        onClick = { viewModel.assignToToday(task.id) },
                                        label = { Text("Hoy") },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.CalendarToday,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = CoralAccent.copy(alpha = 0.1f),
                                            labelColor = CoralAccent
                                        )
                                    )
                                    AssistChip(
                                        onClick = { showDatePickerForTaskId = task.id },
                                        label = { Text("Elegir fecha") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            containerColor = NavyLight.copy(alpha = 0.1f),
                                            labelColor = NavyMid
                                        )
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    // Date picker dialog
    showDatePickerForTaskId?.let { taskId ->
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePickerForTaskId = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                            viewModel.assignDate(taskId, date)
                        }
                        showDatePickerForTaskId = null
                    }
                ) { Text("Asignar") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePickerForTaskId = null }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
