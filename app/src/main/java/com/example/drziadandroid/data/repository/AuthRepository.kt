package com.example.drziadandroid.data.repository

import com.example.drziadandroid.data.api.ApiService
import com.example.drziadandroid.data.api.RetrofitClient
import com.example.drziadandroid.data.api.TokenManager
import com.example.drziadandroid.data.model.LoginRequest
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val apiService: ApiService,
    private val tokenManager: TokenManager
) {
    val accessToken: Flow<String?> = tokenManager.accessToken

    suspend fun login(request: LoginRequest): Result<Unit> {
        return try {
            val response = apiService.login(request)
            tokenManager.saveAccessToken(response.access_token)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        tokenManager.clearAccessToken()
        RetrofitClient.clearSession()
    }
}
