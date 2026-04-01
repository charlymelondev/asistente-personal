package com.carlos.asistente.ui.screens.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log

data class TasksUiState(
    val tasks: List<TaskDto> = emptyList(),
    val isLoading: Boolean = false,
    val selectedStatus: String = "pending",
    val selectedCategory: String? = null
)

class TasksViewModel : ViewModel() {

    private val repo = TaskRepository()
    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val tasks = repo.getAllTasks(
                    status = _uiState.value.selectedStatus,
                    category = _uiState.value.selectedCategory
                )
                Log.d("TasksVM", "Got ${tasks.size} tasks: ${tasks.map { it.title }}")
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                Log.e("TasksVM", "Error fetching tasks", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun setStatusFilter(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
        refresh()
    }

    fun setCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        refresh()
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
