package com.carlos.asistente.ui.screens.detail

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

data class DetailUiState(
    val task: TaskDto? = null,
    val isLoading: Boolean = false,
    val deleted: Boolean = false,
    val error: String? = null,
    val showDoneCelebration: Boolean = false
)

class DetailViewModel : ViewModel() {

    private val repo = TaskRepository()
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val tasks = repo.getAllTasks()
            val task = tasks.find { it.id == taskId }
            _uiState.update { it.copy(task = task, isLoading = false) }
        }
    }

    fun markDone() {
        val id = _uiState.value.task?.id ?: return
        viewModelScope.launch {
            val updated = repo.updateTask(id, UpdateTaskRequest(status = "done"))
            if (updated != null) {
                _uiState.update { it.copy(task = updated, showDoneCelebration = true) }
            }
        }
    }

    fun dismissDoneCelebration() {
        _uiState.update { it.copy(showDoneCelebration = false) }
    }

    fun deleteTask() {
        val id = _uiState.value.task?.id ?: return
        viewModelScope.launch {
            if (repo.deleteTask(id)) {
                _uiState.update { it.copy(deleted = true) }
            }
        }
    }

    fun updateTask(update: UpdateTaskRequest) {
        val id = _uiState.value.task?.id ?: return
        viewModelScope.launch {
            val updated = repo.updateTask(id, update)
            if (updated != null) {
                _uiState.update { it.copy(task = updated) }
            }
        }
    }
}
