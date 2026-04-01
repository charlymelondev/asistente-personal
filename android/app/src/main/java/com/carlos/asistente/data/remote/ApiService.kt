package com.carlos.asistente.data.remote

import com.carlos.asistente.data.remote.dto.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @Multipart
    @POST("api/audio")
    suspend fun uploadAudio(@Part audio: MultipartBody.Part): Response<CreateTasksResponse>

    @POST("api/text")
    suspend fun sendText(@Body body: TextRequest): Response<CreateTasksResponse>

    @GET("api/tasks")
    suspend fun getTasks(
        @Query("status") status: String? = null,
        @Query("category") category: String? = null,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null
    ): Response<TaskListResponse>

    @GET("api/tasks/today")
    suspend fun getTasksToday(): Response<TaskListResponse>

    @GET("api/tasks/week")
    suspend fun getTasksWeek(): Response<TaskListResponse>

    @GET("api/tasks/inbox")
    suspend fun getTasksInbox(): Response<TaskListResponse>

    @GET("api/tasks/overdue")
    suspend fun getTasksOverdue(): Response<TaskListResponse>

    @GET("api/tasks/summary")
    suspend fun getSummary(): Response<SummaryResponse>

    @PATCH("api/tasks/{id}")
    suspend fun updateTask(
        @Path("id") id: String,
        @Body body: UpdateTaskRequest
    ): Response<TaskResponse>

    @DELETE("api/tasks/{id}")
    suspend fun deleteTask(@Path("id") id: String): Response<DeleteResponse>

    @GET("api/health")
    suspend fun health(): Response<Map<String, Any>>
}
