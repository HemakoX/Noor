package com.example.drziadandroid.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.drziadandroid.data.model.*
import com.example.drziadandroid.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

// ─── UI State ─────────────────────────────────────────────────────────────────

data class PatientProfileUiState(
    // Core data
    val isLoading: Boolean = false,
    val profile: PatientProfileResponse? = null,
    val error: String? = null,

    // Per-section saving indicators
    val isSavingBasicInfo: Boolean = false,
    val isSavingMedicalHistory: Boolean = false,
    val isSavingBaseline: Boolean = false,
    val isSavingInjection: Boolean = false,
    val isDeletingInjection: Boolean = false,

    // Per-section save errors
    val saveBasicInfoError: String? = null,
    val saveMedicalHistoryError: String? = null,
    val saveBaselineError: String? = null,
    val saveInjectionError: String? = null,
    val deleteInjectionError: String? = null,

    // Editing visibility
    val isEditingBasicInfo: Boolean = false,
    val isEditingMedicalHistory: Boolean = false,
    val isAddingBaseline: Boolean = false,
    val isAddingInjection: Boolean = false,
    val addingFollowUpToInjectionId: String? = null,
    val isAddingDiagnosis: Boolean = false,

    // Injection to confirm delete
    val injectionPendingDeleteId: String? = null
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

class PatientProfileViewModel(private val repository: PatientRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PatientProfileUiState())
    val uiState: StateFlow<PatientProfileUiState> = _uiState.asStateFlow()

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadProfile(code: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            repository.getPatientProfile(code)
                .onSuccess { profile ->
                    _uiState.update { it.copy(isLoading = false, profile = profile) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = formatError(error)
                        )
                    }
                }
        }
    }

    // ── Editing visibility ────────────────────────────────────────────────────

    fun setEditingBasicInfo(isEditing: Boolean) {
        _uiState.update { it.copy(isEditingBasicInfo = isEditing, saveBasicInfoError = null) }
    }

    fun setEditingMedicalHistory(isEditing: Boolean) {
        _uiState.update { it.copy(isEditingMedicalHistory = isEditing, saveMedicalHistoryError = null) }
    }

    fun setAddingBaseline(isAdding: Boolean) {
        _uiState.update { it.copy(isAddingBaseline = isAdding, saveBaselineError = null) }
    }

    fun setAddingInjection(isAdding: Boolean) {
        _uiState.update { it.copy(isAddingInjection = isAdding, saveInjectionError = null) }
    }

    fun setAddingFollowUp(injectionId: String?) {
        _uiState.update { it.copy(addingFollowUpToInjectionId = injectionId, saveInjectionError = null) }
    }

    fun setAddingDiagnosis(isAdding: Boolean) {
        _uiState.update { it.copy(isAddingDiagnosis = isAdding) }
    }

    fun requestDeleteInjection(injectionId: String) {
        _uiState.update { it.copy(injectionPendingDeleteId = injectionId, deleteInjectionError = null) }
    }

    fun cancelDeleteInjection() {
        _uiState.update { it.copy(injectionPendingDeleteId = null) }
    }

    // ── Save: Patient Basic Info ──────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun saveBasicInfo(name: String, phone: String, age: Int, gender: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingBasicInfo = true, saveBasicInfoError = null) }

            // TODO: Wire to real endpoint when available:
            // val request = UpdatePatientRequest(name, phone, age, gender)
            // repository.updatePatient(profile.code, request)
            //   .onSuccess { updated -> _uiState.update { it.copy(profile = updated, ...) } }
            //   .onFailure { ... }

            // UI-only: update local state immediately
            _uiState.update { state ->
                val updated = state.profile?.copy(
                    name = name.takeIf { it.isNotBlank() },
                    phone = phone.takeIf { it.isNotBlank() },
                    age = age,
                    gender = gender
                )
                state.copy(
                    profile = updated,
                    isSavingBasicInfo = false,
                    isEditingBasicInfo = false
                )
            }
        }
    }

    // ── Save: Medical History ─────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun saveMedicalHistory(diabetes: Boolean, hypertension: Boolean, hba1c: Double?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingMedicalHistory = true, saveMedicalHistoryError = null) }

            // TODO: Wire to real endpoint when available:
            // val request = UpdateMedicalHistoryRequest(diabetes, hypertension, hba1c)
            // repository.updateMedicalHistory(profile.id, request)

            // UI-only
            _uiState.update { state ->
                val updatedHistory = state.profile?.medical_history?.copy(
                    diabetes = diabetes,
                    hypertension = hypertension,
                    hba1c = hba1c
                ) ?: MedicalHistoryResponse(
                    diabetes = diabetes,
                    hypertension = hypertension,
                    hba1c = hba1c
                )
                state.copy(
                    profile = state.profile?.copy(medical_history = updatedHistory),
                    isSavingMedicalHistory = false,
                    isEditingMedicalHistory = false
                )
            }
        }
    }

    // ── Add: Baseline Eye ─────────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun addBaseline(eye: BaseEyeResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingBaseline = true, saveBaselineError = null) }

            // TODO: Wire to real endpoint when available.

            _uiState.update { state ->
                val updated = (state.profile?.base_eyes ?: emptyList()) + eye
                state.copy(
                    profile = state.profile?.copy(base_eyes = updated),
                    isSavingBaseline = false,
                    isAddingBaseline = false
                )
            }
        }
    }

    // ── Add: Injection ────────────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun addInjection(injection: InjectionResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingInjection = true, saveInjectionError = null) }

            // TODO: Wire to real endpoint when available.

            _uiState.update { state ->
                val updated = (state.profile?.injections ?: emptyList()) + injection
                state.copy(
                    profile = state.profile?.copy(injections = updated),
                    isSavingInjection = false,
                    isAddingInjection = false
                )
            }
        }
    }

    // ── Add: Follow-up ────────────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun addFollowUp(injectionId: String, followUp: PostInjectionResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingInjection = true, saveInjectionError = null) }

            // TODO: Wire to real endpoint when available.

            _uiState.update { state ->
                val updatedInjections = state.profile?.injections?.map { inj ->
                    if (inj.id == injectionId) inj.copy(post_injection = followUp) else inj
                } ?: emptyList()
                state.copy(
                    profile = state.profile?.copy(injections = updatedInjections),
                    isSavingInjection = false,
                    addingFollowUpToInjectionId = null
                )
            }
        }
    }

    // ── Delete: Injection ─────────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun confirmDeleteInjection() {
        val injectionId = _uiState.value.injectionPendingDeleteId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeletingInjection = true, deleteInjectionError = null) }

            // TODO: Wire to real endpoint when available:
            // repository.deleteInjection(injectionId)
            //   .onSuccess { ... }
            //   .onFailure { err -> _uiState.update { it.copy(isDeletingInjection = false, deleteInjectionError = formatError(err)) } }

            // UI-only
            _uiState.update { state ->
                val updated = state.profile?.injections?.filter { it.id != injectionId } ?: emptyList()
                state.copy(
                    profile = state.profile?.copy(injections = updated),
                    isDeletingInjection = false,
                    injectionPendingDeleteId = null
                )
            }
        }
    }

    // ── Add: Ocular Diagnosis ─────────────────────────────────────────────────
    // UI-ONLY: No backend endpoint confirmed. Architecture is ready for real API.

    fun addOcularDiagnosis(diagnosisName: String, eye: String?) {
        viewModelScope.launch {
            // TODO: Wire to real catalog endpoint when available.
            val newDiagnosis = OcularDiagnosisResponse(
                id = UUID.randomUUID().toString(),
                patient_id = _uiState.value.profile?.id ?: "",
                eye = eye,
                primary_diagnosis_id = UUID.randomUUID().toString(),
                primary_diagnosis_name = diagnosisName
            )
            _uiState.update { state ->
                val updated = (state.profile?.ocular_diagnoses ?: emptyList()) + newDiagnosis
                state.copy(
                    profile = state.profile?.copy(ocular_diagnoses = updated),
                    isAddingDiagnosis = false
                )
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatError(error: Throwable): String {
        return when {
            error.message?.contains("Unable to resolve host") == true ->
                "Network error — check your connection"
            error.message?.contains("401") == true ->
                "Session expired — please log in again"
            error.message?.contains("404") == true ->
                "Patient not found"
            error.message?.contains("timeout") == true ->
                "Request timed out — please retry"
            else -> error.message ?: "An unexpected error occurred"
        }
    }
}
