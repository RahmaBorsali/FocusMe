package com.example.focusme.data.api

import android.content.Context
import com.example.focusme.data.local.TokenStore
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object ApiClient {

    fun createRetrofit(context: Context): Retrofit {
        val tokenStore = TokenStore(context)
        val candidateBaseUrls = ApiConfig.candidateBaseHttpUrls()

        val authInterceptor = Interceptor { chain ->
            val reqBuilder = chain.request().newBuilder()
            val token = tokenStore.getTokenBlocking()
            if (!token.isNullOrBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }
            chain.proceed(reqBuilder.build())
        }

        val fallbackBaseUrlInterceptor = Interceptor { chain ->
            val request = chain.request()
            val requestUrl = request.url
            var lastException: IOException? = null

            for (baseUrl in candidateBaseUrls) {
                val nextRequest = request.newBuilder()
                    .url(requestUrl.retargetTo(baseUrl))
                    .build()

                try {
                    return@Interceptor chain.proceed(nextRequest)
                } catch (error: IOException) {
                    lastException = error
                }
            }

            throw lastException ?: IOException(ApiConfig.connectionHelpMessage())
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(fallbackBaseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()

        return Retrofit.Builder()
            .baseUrl(ApiConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun authApi(context: Context): AuthApi =
        createRetrofit(context).create(AuthApi::class.java)

    fun friendsApi(context: Context): FriendsApi =
        createRetrofit(context).create(FriendsApi::class.java)

    fun socialApi(context: Context): FocusMeApiService =
        createRetrofit(context).create(FocusMeApiService::class.java)

    fun chatApi(context: Context): ChatApi =
        createRetrofit(context).create(ChatApi::class.java)

    fun challengesApi(context: Context): ChallengesApi =
        createRetrofit(context).create(ChallengesApi::class.java)
}

private fun HttpUrl.retargetTo(baseUrl: HttpUrl): HttpUrl =
    newBuilder()
        .scheme(baseUrl.scheme)
        .host(baseUrl.host)
        .port(baseUrl.port)
        .build()
