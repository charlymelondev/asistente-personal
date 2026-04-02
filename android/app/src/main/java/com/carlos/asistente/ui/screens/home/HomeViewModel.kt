package com.carlos.asistente.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.asistente.data.remote.dto.SummaryResponse
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val isProcessing: Boolean = false,
    val todayTasks: List<TaskDto> = emptyList(),
    val allPendingTasks: List<TaskDto> = emptyList(),
    val overdueTasks: List<TaskDto> = emptyList(),
    val summary: SummaryResponse? = null,
    val lastCreatedTasks: List<TaskDto>? = null,
    val lastTranscript: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val showCreateCelebration: Boolean = false,
    val showDoneCelebration: Boolean = false
)

class HomeViewModel : ViewModel() {

    private val repo = TaskRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val today = repo.getTasksToday()
                val allPending = repo.getAllTasks(status = "pending")
                val overdue = repo.getTasksOverdue()
                val summary = repo.getSummary()
                _uiState.update {
                    it.copy(
                        todayTasks = today,
                        allPendingTasks = allPending,
                        overdueTasks = overdue,
                        summary = summary,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = "Error al cargar tareas: ${e.message}", isLoading = false)
                }
            }
        }
    }

    fun sendText(text: String) {
        _uiState.update { it.copy(isProcessing = true, error = null, lastCreatedTasks = null) }

        viewModelScope.launch {
            try {
                val result = repo.sendText(text)
                if (result != null) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            lastCreatedTasks = result.tasks,
                            lastTranscript = null,
                            showCreateCelebration = result.tasks.isNotEmpty()
                        )
                    }
                    refresh()
                } else {
                    _uiState.update {
                        it.copy(isProcessing = false, error = "Error al procesar texto")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = "Error: ${e.message}")
                }
            }
        }
    }

    fun markDone(taskId: String) {
        viewModelScope.launch {
            if (repo.markDone(taskId)) {
                _uiState.update { it.copy(showDoneCelebration = true) }
                refresh()
            }
        }
    }

    fun dismissCreateCelebration() {
        _uiState.update { it.copy(showCreateCelebration = false) }
    }

    fun dismissDoneCelebration() {
        _uiState.update { it.copy(showDoneCelebration = false) }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            if (repo.deleteTask(taskId)) {
                refresh()
            }
        }
    }

    fun clearLastResult() {
        _uiState.update { it.copy(lastCreatedTasks = null, lastTranscript = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
