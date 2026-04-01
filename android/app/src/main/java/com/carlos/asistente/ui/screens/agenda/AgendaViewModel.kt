package com.carlos.asistente.ui.screens.agenda

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgendaUiState(
    val weekTasks: Map<String, List<TaskDto>> = emptyMap(),
    val isLoading: Boolean = false
)

class AgendaViewModel : ViewModel() {

    private val repo = TaskRepository()
    private val _uiState = MutableStateFlow(AgendaUiState())
    val uiState: StateFlow<AgendaUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tasks = repo.getTasksWeek()
            val grouped = tasks.groupBy { it.due_date ?: "Sin fecha" }
            _uiState.update { it.copy(weekTasks = grouped, isLoading = false) }
        }
    }

    fun markDone(taskId: String) {
        viewModelScope.launch {
            if (repo.markDone(taskId)) refresh()
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            if (repo.deleteTask(taskId)) refresh()
        }
    }
}
