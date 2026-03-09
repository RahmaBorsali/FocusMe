package com.example.focusme.data.api.dto

import com.google.gson.annotations.SerializedName

data class TaskCreateRequest(
    @SerializedName("title") val title: String,
    @SerializedName("userId") val userId: String? = null,
    @SerializedName("isDone") val isDone: Boolean = false,
    @SerializedName("dueDate") val dueDate: String? = null
)

data class TaskCreateResponse(
    @SerializedName("_id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("isDone") val isDone: Boolean,
    @SerializedName("dueDate") val dueDate: String?
)
