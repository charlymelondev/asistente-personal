package com.carlos.asistente.data.remote.dto

data class VersionResponse(
    val version: String,
    val versionCode: Int,
    val downloadUrl: String
)
