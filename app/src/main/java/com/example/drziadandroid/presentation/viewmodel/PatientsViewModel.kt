package com.example.drziadandroid.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drziadandroid.data.model.PatientResponse
import com.example.drziadandroid.data.repository.PatientRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PatientsUiState(
    val isLoading: Boolean = false,
    val patients: List<PatientResponse> = emptyList(),
    val todayPatients: List<PatientResponse> = emptyList(),
    val error: String? = null,
    val searchQuery: String = "",
    val isTodayOnly: Boolean = false
)

class PatientsViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientsUiState())
    val uiState: StateFlow<PatientsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadPatients()
        loadTodayPatients()
    }

    fun loadPatients(query: String? = _uiState.value.searchQuery) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = repository.getPatients(page = 1, pageSize = 50, query = query)
            result.onSuccess { response ->
                _uiState.update { it.copy(isLoading = false, patients = response.patients) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Failed to load patients") }
            }
        }
    }

    fun loadTodayPatients() {
        viewModelScope.launch {
            val result = repository.getTodayPatients()
            result.onSuccess { patients ->
                _uiState.update { it.copy(todayPatients = patients) }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            loadPatients(query)
        }
    }

    fun toggleTodayOnly(todayOnly: Boolean) {
        _uiState.update { it.copy(isTodayOnly = todayOnly) }
    }
}
