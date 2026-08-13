package com.example.drziadandroid.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drziadandroid.data.api.RetrofitClient
import com.example.drziadandroid.data.repository.AuthRepository
import com.example.drziadandroid.data.repository.PatientRepository

class ViewModelFactory : ViewModelProvider.Factory {
    private val authRepository = AuthRepository(
        RetrofitClient.apiService,
        RetrofitClient.getTokenManager()
    )

    private val patientRepository = PatientRepository(RetrofitClient.apiService)

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                LoginViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(MainViewModel::class.java) -> {
                MainViewModel(authRepository) as T
            }
            modelClass.isAssignableFrom(PatientsViewModel::class.java) -> {
                PatientsViewModel(patientRepository) as T
            }
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> {
                DashboardViewModel(patientRepository) as T
            }
            modelClass.isAssignableFrom(PatientProfileViewModel::class.java) -> {
                PatientProfileViewModel(patientRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
