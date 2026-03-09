package com.example.focusme.data.api.dto

import com.google.gson.annotations.SerializedName

data class FeedItem(
    @SerializedName("friendId") val friendId: String,
    @SerializedName("friendName") val friendName: String,
    @SerializedName("avatarUrl") val avatarUrl: String?,
    @SerializedName("actionType") val actionType: String,
    @SerializedName("value") val value: Int,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String  // ISO string from backend "2026-03-07T10:00:00.000Z"
)
