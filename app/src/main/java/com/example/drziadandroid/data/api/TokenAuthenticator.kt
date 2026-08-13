package com.example.drziadandroid.data.api

import com.example.drziadandroid.BuildConfig
import com.example.drziadandroid.data.model.AuthResponse
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor

class TokenAuthenticator(
    private val tokenManager: TokenManager,
    private val cookieJar: CookieJar,
    private val json: Json
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Only retry once. If priorResponse is not null, we've already tried to authenticate.
        if (response.priorResponse != null) {
            return null
        }

        // Don't attempt to refresh if the failed request was the refresh request itself
        if (response.request.url.encodedPath.contains("api/v1/admin/refresh")) {
            return null
        }

        synchronized(this) {
            val currentToken = runBlocking { tokenManager.accessToken.firstOrNull() }
            val requestToken = response.request.header("Authorization")?.removePrefix("Bearer ")

            // If the token in the request is different from the current token, 
            // another thread already refreshed it.
            if (!currentToken.isNullOrBlank() && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            // Refresh the token
            val newToken = refreshToken()

            return if (!newToken.isNullOrBlank()) {
                response.request.newBuilder()
                    .header("Authorization", "Bearer $newToken")
                    .build()
            } else {
                null
            }
        }
    }

    private fun refreshToken(): String? {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.HEADERS else HttpLoggingInterceptor.Level.NONE
            redactHeader("Authorization")
            redactHeader("Cookie")
            redactHeader("Set-Cookie")
        }

        // Dedicated client for refresh to avoid interceptor/authenticator recursion.
        // It must use the same persistent cookieJar to send the refresh_token.
        val refreshClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .cookieJar(cookieJar)
            .build()

        val request = Request.Builder()
            .url(BuildConfig.BASE_URL + "api/v1/admin/refresh")
            .post("{}".toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = refreshClient.newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: return null
                val authResponse = json.decodeFromString<AuthResponse>(body)
                runBlocking {
                    tokenManager.saveAccessToken(authResponse.access_token)
                }
                authResponse.access_token
            } else {
                handleFailure()
                null
            }
        } catch (e: Exception) {
            handleFailure()
            null
        }
    }

    private fun handleFailure() {
        runBlocking {
            tokenManager.clearAccessToken()
            RetrofitClient.clearSession()
        }
    }
}
