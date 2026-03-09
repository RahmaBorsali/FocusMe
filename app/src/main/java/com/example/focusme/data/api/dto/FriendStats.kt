package com.example.focusme.data.api.dto

import com.google.gson.annotations.SerializedName

data class FriendStats(
    @SerializedName("userId") val userId: String,
    @SerializedName("name") val name: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("weeklyFocusMin") val weeklyFocusMin: Int,
    @SerializedName("tasksThisWeek") val tasksThisWeek: Int,
    @SerializedName("streak") val streak: Int,
    @SerializedName("rank") val rank: Int
)
