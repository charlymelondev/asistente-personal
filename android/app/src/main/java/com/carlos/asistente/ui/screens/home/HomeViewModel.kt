package com.carlos.asistente.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlos.asistente.audio.AudioRecorder
import com.carlos.asistente.data.remote.dto.SummaryResponse
import com.carlos.asistente.data.remote.dto.TaskDto
import com.carlos.asistente.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class HomeUiState(
    val isRecording: Boolean = false,
    val isProcessing: Boolean = false,
    val todayTasks: List<TaskDto> = emptyList(),
    val overdueTasks: List<TaskDto> = emptyList(),
    val summary: SummaryResponse? = null,
    val lastCreatedTasks: List<TaskDto>? = null,
    val lastTranscript: String? = null,
    val error: String? = null,
    val isLoading: Boolean = false,
    val showCreateCelebration: Boolean = false,
    val showDoneCelebration: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = TaskRepository()
    private val recorder = AudioRecorder(application)
    private var audioFile: File? = null

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
                val overdue = repo.getTasksOverdue()
                val summary = repo.getSummary()
                _uiState.update {
                    it.copy(
                        todayTasks = today,
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

    fun startRecording() {
        try {
            audioFile = recorder.startRecording()
            _uiState.update { it.copy(isRecording = true, error = null) }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Error al grabar: ${e.message}") }
        }
    }

    fun stopRecordingAndSend() {
        val file = recorder.stopRecording()
        _uiState.update { it.copy(isRecording = false) }

        if (file == null || !file.exists() || file.length() == 0L) {
            _uiState.update { it.copy(error = "No se grabó audio") }
            return
        }

        _uiState.update { it.copy(isProcessing = true, error = null, lastCreatedTasks = null) }

        viewModelScope.launch {
            try {
                val result = repo.uploadAudio(file)
                if (result != null) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            lastCreatedTasks = result.tasks,
                            lastTranscript = result.transcript
                        )
                    }
                    refresh()
                } else {
                    _uiState.update {
                        it.copy(isProcessing = false, error = "Error al procesar audio")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isProcessing = false, error = "Error: ${e.message}")
                }
            } finally {
                file.delete()
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
