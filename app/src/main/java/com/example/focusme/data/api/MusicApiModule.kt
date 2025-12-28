package com.example.focusme.data.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MusicApiModule {

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: MusicApi by lazy {
        retrofit.create(MusicApi::class.java)
    }
}
