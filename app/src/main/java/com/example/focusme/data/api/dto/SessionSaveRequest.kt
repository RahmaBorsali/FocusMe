package com.example.focusme.data.api.dto

import com.google.gson.annotations.SerializedName

data class SessionSaveRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("durationMinutes") val durationMinutes: Int,
    @SerializedName("taskIds") val taskIds: List<String> = emptyList(),
    @SerializedName("completedTaskTitles") val completedTaskTitles: List<String> = emptyList(),
    @SerializedName("date") val date: String
)


