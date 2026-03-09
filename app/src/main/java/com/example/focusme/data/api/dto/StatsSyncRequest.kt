package com.example.focusme.data.api.dto

import com.google.gson.annotations.SerializedName

data class StatsSyncRequest(
    @SerializedName("userId") val userId: String,
    @SerializedName("date") val date: String,
    @SerializedName("focusMinutes") val focusMinutes: Int,
    @SerializedName("sessionsCount") val sessionsCount: Int,
    @SerializedName("tasksCompleted") val tasksCompleted: Int,
    @SerializedName("streak") val streak: Int
)
