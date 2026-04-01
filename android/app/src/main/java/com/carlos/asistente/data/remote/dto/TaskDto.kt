package com.carlos.asistente.data.remote.dto

data class TaskDto(
    val id: String,
    val title: String,
    val description: String?,
    val category: String,
    val priority: String,
    val due_date: String?,
    val due_time: String?,
    val status: String,
    val done_at: String?,
    val source_text: String?,
    val created_at: String,
    val updated_at: String
)

data class TaskListResponse(
    val tasks: List<TaskDto>,
    val total: Int
)

data class CreateTasksResponse(
    val transcript: String?,
    val tasks: List<TaskDto>
)

data class TaskResponse(
    val task: TaskDto
)

data class DeleteResponse(
    val deleted: Boolean
)

data class TextRequest(
    val text: String
)

data class UpdateTaskRequest(
    val status: String? = null,
    val title: String? = null,
    val description: String? = null,
    val category: String? = null,
    val priority: String? = null,
    val due_date: String? = null,
    val due_time: String? = null
)

data class SummaryResponse(
    val total_pending: Int,
    val by_category: Map<String, Int>,
    val overdue: Int,
    val today: Int,
    val this_week: Int,
    val inbox: Int = 0
)
