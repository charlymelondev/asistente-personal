package com.carlos.asistente.ui.screens.tasks

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.carlos.asistente.ui.components.AnimatedAvatar
import com.carlos.asistente.ui.components.AvatarState
import com.carlos.asistente.ui.components.TaskCard
import com.carlos.asistente.ui.components.getCategoryColor
import com.carlos.asistente.ui.theme.NavyDeep

private val CATEGORIES = listOf(
    null to "Todas",
    "gestiones" to "Gestiones",
    "llamadas" to "Llamadas",
    "pagos" to "Pagos",
    "compras" to "Compras",
    "trabajo" to "Trabajo",
    "personal" to "Personal",
    "salud" to "Salud"
)

private val STATUSES = listOf(
    "pending" to "Pendientes",
    "done" to "Hechas",
    "all" to "Todas"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onTaskClick: (String) -> Unit,
    viewModel: TasksViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Refresh every time this screen enters composition (e.g. switching tabs)
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Log.d("TasksScreen", "RENDERED! tasks=${state.tasks.size} loading=${state.isLoading} status=${state.selectedStatus}")

    PullToRefreshBox(
        isRefreshing = state.isLoading,
        onRefresh = { viewModel.refresh() },
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // Header bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NavyDeep)
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Mis tareas",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Status filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    STATUSES.forEach { (value, label) ->
                        StatusFilterChip(
                            label = label,
                            selected = state.selectedStatus == value,
                            onClick = { viewModel.setStatusFilter(value) }
                        )
                    }
                }
            }

            // Category filter chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CATEGORIES.forEach { (value, label) ->
                        CategoryFilterChip(
                            label = label,
                            category = value,
                            selected = state.selectedCategory == value,
                            onClick = { viewModel.setCategoryFilter(value) }
                        )
                    }
                }
            }

            // Empty state
            if (state.tasks.isEmpty() && !state.isLoading) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedAvatar(
                            state = AvatarState.RELAXED,
                            size = 90.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No hay tareas",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Prueba cambiando los filtros",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Task list with staggered enter animation
            itemsIndexed(state.tasks, key = { _, task -> task.id }) { index, task ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(tween(300, delayMillis = index * 40)) + slideInVertically(
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
private fun StatusFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) NavyDeep else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = if (selected) Modifier.shadow(4.dp, RoundedCornerShape(20.dp)) else Modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    category: String?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = if (category != null) getCategoryColor(category) else NavyDeep

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) categoryColor else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = if (selected) Modifier.shadow(2.dp, RoundedCornerShape(20.dp)) else Modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
        ) {
            if (category != null && !selected) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(categoryColor)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
