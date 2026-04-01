package com.carlos.asistente.ui.screens.agenda

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.asistente.ui.components.AnimatedAvatar
import com.carlos.asistente.ui.components.AvatarState
import com.carlos.asistente.ui.components.TaskCard
import com.carlos.asistente.ui.theme.CoralAccent
import com.carlos.asistente.ui.theme.NavyDeep
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgendaScreen(
    onTaskClick: (String) -> Unit,
    viewModel: AgendaViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val today = LocalDate.now()

    // Start selected day on today
    var selectedDay by remember { mutableStateOf(today) }

    // Build the 7-day window starting from today
    val weekDays = remember(today) {
        (0..6).map { today.plusDays(it.toLong()) }
    }

    // Tasks for the selected day
    val selectedDayTasks = remember(state.weekTasks, selectedDay) {
        state.weekTasks[selectedDay.toString()] ?: emptyList()
    }

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with gradient
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDeep)
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 0.dp)
                ) {
                    Text(
                        text = "Agenda",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Weekly calendar row (inside the header box)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDeep)
                        .padding(horizontal = 12.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        weekDays.forEach { day ->
                            DayCell(
                                day = day,
                                isSelected = day == selectedDay,
                                isToday = day == today,
                                hasEvents = state.weekTasks[day.toString()]?.isNotEmpty() == true,
                                onClick = { selectedDay = day }
                            )
                        }
                    }
                }
            }

            // Day title
            item {
                val dayLabel = if (selectedDay == today) "Hoy" else formatDayHeader(selectedDay.toString())
                val taskCount = selectedDayTasks.size

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(16.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(CoralAccent)
                    )
                    Text(
                        text = dayLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (taskCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = CoralAccent.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = taskCount.toString(),
                                style = MaterialTheme.typography.labelSmall,
                                color = CoralAccent,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Tasks for selected day with animation
            if (selectedDayTasks.isEmpty() && !state.isLoading) {
                item {
                    AnimatedContent(
                        targetState = selectedDay,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                        },
                        label = "emptyState"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AnimatedAvatar(
                                state = AvatarState.RELAXED,
                                size = 90.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (it == today) "Sin tareas para hoy" else "Día libre",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (it == today) "¡A disfrutar!" else "Nada programado",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            itemsIndexed(selectedDayTasks, key = { _, task -> "${selectedDay}-${task.id}" }) { index, task ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = index * 50)) + slideInVertically(
                        animationSpec = spring(Spring.DampingRatioMediumBouncy),
                        initialOffsetY = { it / 2 }
                    )
                ) {
                    TaskCard(
                        task = task,
                        onDone = { viewModel.markDone(task.id) },
                        onDelete = { viewModel.deleteTask(task.id) },
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    val dayName = day.format(DateTimeFormatter.ofPattern("EEE", Locale("es")))
        .uppercase()
        .take(2)
    val dayNumber = day.dayOfMonth.toString()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                when {
                    isSelected -> CoralAccent
                    else -> Color.Transparent
                }
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isSelected -> Color.White
                isToday -> CoralAccent
                else -> Color.White.copy(alpha = 0.6f)
            },
            fontSize = 10.sp,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White
            else if (isToday) CoralAccent
            else Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        // Event dot
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected && hasEvents -> Color.White
                        hasEvents -> CoralAccent
                        else -> Color.Transparent
                    }
                )
        )
    }
}

private fun formatDayHeader(dateStr: String): String {
    return try {
        val date = LocalDate.parse(dateStr)
        val formatter = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es"))
        date.format(formatter).replaceFirstChar { it.uppercase() }
    } catch (_: Exception) {
        dateStr
    }
}
