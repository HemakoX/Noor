package com.example.drziadandroid.data.api

import com.example.drziadandroid.data.model.AuthResponse
import com.example.drziadandroid.data.model.LoginRequest
import com.example.drziadandroid.data.model.PatientListResponse
import com.example.drziadandroid.data.model.PatientProfileResponse
import com.example.drziadandroid.data.model.PatientResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("api/v1/admin/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @POST("api/v1/admin/refresh")
    suspend fun refresh(): AuthResponse

    @GET("api/v1/patients")
    suspend fun getPatients(
        @Query("page") page: Int,
        @Query("page_size") pageSize: Int,
        @Query("q") query: String? = null
    ): PatientListResponse

    @GET("api/v1/patients/today")
    suspend fun getTodayPatients(): List<PatientResponse>

    @GET("api/v1/patients/{code}/profile")
    suspend fun getPatientProfile(
        @Path("code") code: String
    ): PatientProfileResponse
}
