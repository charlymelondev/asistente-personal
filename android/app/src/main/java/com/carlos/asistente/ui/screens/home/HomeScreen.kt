package com.carlos.asistente.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.asistente.ui.components.AnimatedAvatar
import com.carlos.asistente.ui.components.AvatarState
import com.carlos.asistente.ui.components.CelebrationManager
import com.carlos.asistente.ui.components.NewTaskSheet
import com.carlos.asistente.ui.components.TaskCard
import com.carlos.asistente.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onTaskClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var showNewTask by remember { mutableStateOf(false) }

    LaunchedEffect(state.lastCreatedTasks) {
        if (state.lastCreatedTasks != null) {
            showNewTask = false
        }
    }

    val avatarState = when {
        state.isProcessing -> AvatarState.THINKING
        state.lastCreatedTasks != null -> AvatarState.CELEBRATING
        state.todayTasks.isEmpty() && state.overdueTasks.isEmpty() && !state.isLoading -> AvatarState.RELAXED
        else -> AvatarState.GREETING
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewTask = true },
                containerColor = CoralAccent,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 10.dp
                ),
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Nueva tarea",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        var userTriggeredRefresh by remember { mutableStateOf(false) }
        if (!state.isLoading) userTriggeredRefresh = false
        PullToRefreshBox(
            isRefreshing = userTriggeredRefresh && state.isLoading,
            onRefresh = {
                userTriggeredRefresh = true
                viewModel.refresh()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // === Greeting card with gradient ===
                item {
                    GreetingCard(
                        avatarState = avatarState,
                        pendingCount = state.summary?.total_pending ?: 0,
                        overdueCount = state.summary?.overdue ?: 0
                    )
                }

                // === Summary stats row ===
                state.summary?.let { summary ->
                    item {
                        SummaryStatsRow(
                            todayCount = summary.today,
                            overdueCount = summary.overdue,
                            weekCount = summary.this_week,
                            inboxCount = summary.inbox,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }

                // === Status banners (processing / result) ===
                if (state.isProcessing) {
                    item {
                        StatusBanner(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            text = "Procesando tu mensaje...",
                            showProgress = true
                        )
                    }
                }

                state.lastCreatedTasks?.let { tasks ->
                    item {
                        CreatedTasksBanner(
                            tasks = tasks.map { it.title },
                            transcript = state.lastTranscript,
                            onDismiss = { viewModel.clearLastResult() }
                        )
                    }
                }

                state.error?.let { error ->
                    item {
                        ErrorBanner(
                            message = error,
                            onDismiss = { viewModel.clearError() }
                        )
                    }
                }

                // === All pending tasks ===
                if (state.allPendingTasks.isEmpty() && !state.isLoading && state.lastCreatedTasks == null) {
                    item {
                        EmptyTodayState(
                            avatarState = avatarState,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    }
                }

                itemsIndexed(state.allPendingTasks, key = { _, task -> "pending-${task.id}" }) { index, task ->
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

    if (showNewTask) {
        NewTaskSheet(
            isProcessing = state.isProcessing,
            onSendText = { text ->
                viewModel.sendText(text)
                showNewTask = false
            },
            onDismiss = { showNewTask = false }
        )
    }

    // Celebrations must be AFTER the sheet so they render on top
    CelebrationManager(
        showCreateCelebration = state.showCreateCelebration,
        showDoneCelebration = state.showDoneCelebration,
        onDismissCreate = { viewModel.dismissCreateCelebration() },
        onDismissDone = { viewModel.dismissDoneCelebration() }
    )
}

// ─── Composables auxiliares ──────────────────────────────────────────────────

@Composable
private fun GreetingCard(
    avatarState: AvatarState,
    pendingCount: Int,
    overdueCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(NavyDeep, NavyLight)
                )
            )
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Hola Papí",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "¿En qué puedo ayudarte hoy?",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnNavySecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        overdueCount > 0 -> "$pendingCount pendientes · $overdueCount vencidas"
                        pendingCount > 0 -> "$pendingCount tareas pendientes"
                        else -> "Todo al día. ¡Buen trabajo!"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnNavySecondary
                )
            }

            AnimatedAvatar(
                state = avatarState,
                stickerAsset = "sticker.png",
                size = 80.dp
            )
        }
    }
}

@Composable
private fun SummaryStatsRow(
    todayCount: Int,
    overdueCount: Int,
    weekCount: Int,
    inboxCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val totalPending = todayCount + overdueCount + weekCount
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = totalPending.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = NavyDeep
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Pendientes",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accentColor)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (count > 0) {
            Surface(
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.12f)
            ) {
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyTodayState(
    avatarState: AvatarState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedAvatar(
            state = avatarState,
            stickerAsset = "sticker.png",
            size = 90.dp
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sin tareas para hoy",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pulsa + para crear una tarea",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StatusBanner(
    color: Color,
    onColor: Color,
    text: String,
    showProgress: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (showProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = onColor,
                    strokeWidth = 2.dp
                )
            }
            Text(
                text = text,
                color = onColor,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun CreatedTasksBanner(
    tasks: List<String>,
    transcript: String?,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SuccessGreen.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Tareas creadas (${tasks.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = SuccessGreen
                )
                TextButton(onClick = onDismiss, contentPadding = PaddingValues(0.dp)) {
                    Text("Cerrar", style = MaterialTheme.typography.labelMedium, color = SuccessGreen)
                }
            }
            transcript?.let {
                Text(
                    text = "\"$it\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
                )
            }
            tasks.forEach { title ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(SuccessGreen)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = onDismiss,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Text("OK", color = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}
