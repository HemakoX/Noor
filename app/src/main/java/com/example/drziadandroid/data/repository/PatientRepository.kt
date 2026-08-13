package com.example.drziadandroid.data.repository

import com.example.drziadandroid.data.api.ApiService
import com.example.drziadandroid.data.model.PatientListResponse
import com.example.drziadandroid.data.model.PatientProfileResponse
import com.example.drziadandroid.data.model.PatientResponse

class PatientRepository(private val apiService: ApiService) {

    suspend fun getPatients(page: Int, pageSize: Int, query: String? = null): Result<PatientListResponse> {
        return try {
            Result.success(apiService.getPatients(page, pageSize, query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTodayPatients(): Result<List<PatientResponse>> {
        return try {
            Result.success(apiService.getTodayPatients())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPatientProfile(code: String): Result<PatientProfileResponse> {
        return try {
            Result.success(apiService.getPatientProfile(code))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
