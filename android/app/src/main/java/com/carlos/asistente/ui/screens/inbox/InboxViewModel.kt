package com.carlos.asistente.ui.screens.inbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.data.remote.dto.UpdateTaskRequest
import com.carlos.asistente.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InboxUiState(
    val tasks: List<TaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class InboxViewModel : ViewModel() {

    private val repo = TaskRepository()
    private val _uiState = MutableStateFlow(InboxUiState())
    val uiState: StateFlow<InboxUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tasks = repo.getTasksInbox()
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun assignToToday(taskId: String) {
        viewModelScope.launch {
            val today = java.time.LocalDate.now().toString()
            repo.updateTask(taskId, UpdateTaskRequest(due_date = today))
            refresh()
        }
    }

    fun assignDate(taskId: String, date: String) {
        viewModelScope.launch {
            repo.updateTask(taskId, UpdateTaskRequest(due_date = date))
            refresh()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            if (repo.deleteTask(taskId)) refresh()
        }
    }

    fun markDone(taskId: String) {
        viewModelScope.launch {
            if (repo.markDone(taskId)) refresh()
        }
    }
}
