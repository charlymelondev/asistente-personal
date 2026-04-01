package com.carlos.asistente.data.repository

import android.util.Log
import com.carlos.asistente.data.remote.ApiClient
import com.carlos.asistente.data.remote.dto.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class TaskRepository {

    private val api = ApiClient.api

    suspend fun uploadAudio(file: File): CreateTasksResponse? {
        val requestBody = file.asRequestBody("audio/mp4".toMediaType())
        val part = MultipartBody.Part.createFormData("audio", file.name, requestBody)
        val response = api.uploadAudio(part)
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun sendText(text: String): CreateTasksResponse? {
        val response = api.sendText(TextRequest(text))
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun getTasksToday(): List<TaskDto> {
        val response = api.getTasksToday()
        return if (response.isSuccessful) response.body()?.tasks ?: emptyList() else emptyList()
    }

    suspend fun getTasksWeek(): List<TaskDto> {
        val response = api.getTasksWeek()
        return if (response.isSuccessful) response.body()?.tasks ?: emptyList() else emptyList()
    }

    suspend fun getTasksInbox(): List<TaskDto> {
        val response = api.getTasksInbox()
        return if (response.isSuccessful) response.body()?.tasks ?: emptyList() else emptyList()
    }

    suspend fun getTasksOverdue(): List<TaskDto> {
        val response = api.getTasksOverdue()
        return if (response.isSuccessful) response.body()?.tasks ?: emptyList() else emptyList()
    }

    suspend fun getAllTasks(
        status: String? = null,
        category: String? = null
    ): List<TaskDto> {
        Log.d("TaskRepo", "getAllTasks called: status=$status category=$category")
        val response = api.getTasks(status = status, category = category)
        Log.d("TaskRepo", "Response: isSuccessful=${response.isSuccessful} code=${response.code()}")
        val body = response.body()
        Log.d("TaskRepo", "Body: tasks=${body?.tasks?.size} total=${body?.total}")
        if (!response.isSuccessful) {
            Log.e("TaskRepo", "Error body: ${response.errorBody()?.string()}")
        }
        return body?.tasks ?: emptyList()
    }

    suspend fun getSummary(): SummaryResponse? {
        val response = api.getSummary()
        return if (response.isSuccessful) response.body() else null
    }

    suspend fun markDone(id: String): Boolean {
        val response = api.updateTask(id, UpdateTaskRequest(status = "done"))
        return response.isSuccessful
    }

    suspend fun deleteTask(id: String): Boolean {
        val response = api.deleteTask(id)
        return response.isSuccessful
    }

    suspend fun updateTask(id: String, update: UpdateTaskRequest): TaskDto? {
        val response = api.updateTask(id, update)
        return if (response.isSuccessful) response.body()?.task else null
    }
}
