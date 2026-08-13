package com.example.drziadandroid.data.api

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val tokenManager: TokenManager) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        // Skip adding Authorization header for refresh request
        if (request.url.encodedPath.contains("api/v1/admin/refresh")) {
            return chain.proceed(request)
        }

        val token = runBlocking {
            tokenManager.accessToken.firstOrNull()
        }

        val newRequest = request.newBuilder().apply {
            if (!token.isNullOrBlank()) {
                addHeader("Authorization", "Bearer $token")
            }
        }.build()

        return chain.proceed(newRequest)
    }
}
