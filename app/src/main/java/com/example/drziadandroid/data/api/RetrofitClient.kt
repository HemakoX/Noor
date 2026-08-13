package com.example.drziadandroid.data.api

import android.content.Context
import com.example.drziadandroid.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitClient {

    private lateinit var tokenManager: TokenManager
    private lateinit var cookieJar: SessionCookieJar
    private var isInitialized = false

    fun init(context: Context) {
        if (isInitialized) return
        tokenManager = TokenManager(context.applicationContext)
        cookieJar = SessionCookieJar(context.applicationContext)
        isInitialized = true
    }

    private fun checkInitialized() {
        if (!isInitialized) {
            throw IllegalStateException("RetrofitClient must be initialized in Application.onCreate()")
        }
    }

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        checkInitialized()
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.HEADERS
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
            // Redact sensitive information
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }

        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(AuthInterceptor(tokenManager))
            .authenticator(TokenAuthenticator(tokenManager, cookieJar, json))
            .cookieJar(cookieJar)
            .build()
    }

    val apiService: ApiService by lazy {
        checkInitialized()
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApiService::class.java)
    }

    fun getTokenManager(): TokenManager {
        checkInitialized()
        return tokenManager
    }

    fun clearSession() {
        checkInitialized()
        cookieJar.clear()
    }
}
