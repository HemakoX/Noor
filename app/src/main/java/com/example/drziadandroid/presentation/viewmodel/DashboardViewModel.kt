package com.example.drziadandroid.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drziadandroid.data.model.PatientResponse
import com.example.drziadandroid.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val todayPatients: List<PatientResponse> = emptyList(),
    val totalPatients: Int = 0,
    val pendingAppointments: Int = 0,
    val newPatientsThisMonth: Int = 0,
    val error: String? = null,
    val searchQuery: String = ""
)

class DashboardViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val todayResult = repository.getTodayPatients()
            val totalResult = repository.getPatients(1, 1) // Get total from metadata

            todayResult.onSuccess { todayList ->
                _uiState.update { state ->
                    state.copy(
                        todayPatients = todayList,
                        // Using placeholders for stats not directly provided by current API endpoints
                        pendingAppointments = todayList.size, 
                        isLoading = false
                    )
                }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message) }
            }

            totalResult.onSuccess { totalRes ->
                _uiState.update { it.copy(totalPatients = totalRes.total) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }
}
