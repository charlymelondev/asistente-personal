package com.carlos.asistente.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: TaskDto,
    onDone: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDone = task.status == "done"

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (!isDone) onDone()
                    false // don't dismiss, VM will remove from list
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                else -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.35f }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            SwipeBackground(dismissState = dismissState)
        },
        content = {
            TaskCardContent(
                task = task,
                isDone = isDone,
                onClick = onClick
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeBackground(dismissState: SwipeToDismissBoxState) {
    val direction = dismissState.dismissDirection
    val fraction = dismissState.progress

    val isSwipingRight = direction == SwipeToDismissBoxValue.StartToEnd
    val isSwipingLeft = direction == SwipeToDismissBoxValue.EndToStart

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSwipingRight -> SwipeCompleteGreen
            isSwipingLeft -> SwipeDeleteRed
            else -> Color.Transparent
        },
        label = "swipeBg"
    )

    val iconScale by animateFloatAsState(
        targetValue = if (fraction > 0.1f) 1f else 0f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "iconScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = if (isSwipingRight) Alignment.CenterStart else Alignment.CenterEnd
    ) {
        if (isSwipingRight) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Completar",
                tint = Color.White,
                modifier = Modifier.scale(iconScale).size(28.dp)
            )
        } else if (isSwipingLeft) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Eliminar",
                tint = Color.White,
                modifier = Modifier.scale(iconScale).size(28.dp)
            )
        }
    }
}

@Composable
private fun TaskCardContent(
    task: TaskDto,
    isDone: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Main content
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Urgent dot
                    if (task.priority == "urgent") {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(PriorityUrgent)
                        )
                    }

                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium
                        ),
                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Priority badge (high only — urgent shown as dot above)
                    if (task.priority == "high") {
                        PriorityBadge(priority = task.priority)
                    }

                    // Due date
                    task.due_date?.let { date ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formatDate(date, task.due_time),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
fun CategoryChip(category: String, color: Color = getCategoryColor(category)) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = category,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun PriorityBadge(priority: String) {
    val (color, label) = when (priority) {
        "urgent" -> PriorityUrgent to "URGENTE"
        "high" -> PriorityHigh to "ALTA"
        else -> return
    }
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.12f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
        )
    }
}

fun getCategoryColor(category: String): Color = when (category) {
    "gestiones" -> CategoryGestiones
    "llamadas" -> CategoryLlamadas
    "pagos" -> CategoryPagos
    "compras" -> CategoryCompras
    "trabajo" -> CategoryTrabajo
    "personal" -> CategoryPersonal
    "salud" -> CategorySalud
    else -> CategoryGeneral
}

fun formatDate(date: String, time: String?): String {
    return try {
        val parts = date.split("-")
        val day = parts[2].toInt()
        val months = listOf("", "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
        val month = months[parts[1].toInt()]
        val timeStr = time?.substring(0, 5)?.let { " $it" } ?: ""
        "$day $month$timeStr"
    } catch (_: Exception) {
        date
    }
}
