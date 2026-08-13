package com.example.drziadandroid.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PatientResponse(
    val id: String,
    val code: String,
    val name: String? = null,
    val phone: String? = null,
    val gender: String, // male|female
    val age: Int,
    val date: String? = null, // YYYY-MM-DD
    val time: String? = null  // HH:MM:SS
)

@Serializable
data class PatientListResponse(
    val patients: List<PatientResponse>,
    val total: Int,
    val page: Int,
    val page_size: Int
)

@Serializable
data class PatientProfileResponse(
    val id: String,
    val code: String,
    val name: String? = null,
    val phone: String? = null,
    val gender: String,
    val age: Int,
    val date: String? = null,
    val time: String? = null,
    val medical_history: MedicalHistoryResponse? = null,
    val base_eyes: List<BaseEyeResponse> = emptyList(),
    val injections: List<InjectionResponse> = emptyList(),
    val ocular_diagnoses: List<OcularDiagnosisResponse> = emptyList()
)

@Serializable
data class MedicalHistoryResponse(
    val id: String? = null,
    val diabetes: Boolean = false,
    val hypertension: Boolean = false,
    val hba1c: Double? = null
)

@Serializable
data class BaseEyeResponse(
    val id: String? = null,
    val eye: String? = null, // OD|OS|OU
    val visual_acuity: String? = null,
    val va_snellen: String? = null,
    val va_logmar: Double? = null,
    val iop: Double? = null,
    val lens_status: String? = null,
    val central_macular_thickness: Double? = null,
    val primary_diagnosis: String? = null,
    val retinal_findings: String? = null,
    val has_previous_history: Boolean = false,
    val previous_history: PreviousHistoryResponse? = null
)

@Serializable
data class PreviousHistoryResponse(
    val id: String? = null,
    val previous_intravitreal_treatment: Boolean = false,
    val number_of_previous_injections: Int? = null,
    val date_of_last_injection: String? = null,
    val previous_agent_name: String? = null
)

@Serializable
data class InjectionResponse(
    val id: String,
    val eye: String? = null,
    val expected_examination_date: String? = null,
    val cmt: Double? = null,
    @SerialName("visual_activity")
    val visualActivity: Double? = null, // Note: The contract says visual_activity in one place and visual_activiy in another. I'll check the other one.
    val va_snellen: String? = null,
    val va_logmar: Double? = null,
    val drug_name: String? = null,
    val protocol_name: String? = null,
    val images: List<ImageResponse> = emptyList(),
    val post_injection: PostInjectionResponse? = null
)

@Serializable
data class PostInjectionResponse(
    val id: String? = null,
    val examination_date: String? = null,
    @SerialName("visual_activiy") // Backend typo preserved
    val visualActivity: Double? = null,
    val va_snellen: String? = null,
    val va_logmar: Double? = null,
    val iop: Double? = null,
    @SerialName("centeral_macular_thickness") // Backend typo preserved
    val centralMacularThickness: Double? = null,
    val notes: String? = null,
    val lens_status: String? = null,
    val images: List<ImageResponse> = emptyList()
)

@Serializable
data class ImageResponse(
    val id: String,
    val url: String
)

@Serializable
data class OcularDiagnosisResponse(
    val id: String,
    val patient_id: String,
    val eye: String? = null,
    val primary_diagnosis_id: String,
    val primary_diagnosis_name: String
)

// ─── Request Models ────────────────────────────────────────────────────────────
// Ready to wire to real endpoints when the backend provides them.

@Serializable
data class UpdatePatientRequest(
    val name: String?,
    val phone: String?,
    val age: Int,
    val gender: String
)

@Serializable
data class UpdateMedicalHistoryRequest(
    val diabetes: Boolean,
    val hypertension: Boolean,
    val hba1c: Double?
)

@Serializable
data class CreateBaselineRequest(
    val eye: String,
    val visual_acuity: String?,
    val va_snellen: String?,
    val va_logmar: Double?,
    val iop: Double?,
    val lens_status: String?,
    val central_macular_thickness: Double?,
    val primary_diagnosis: String?,
    val retinal_findings: String?
)

@Serializable
data class CreateInjectionRequest(
    val eye: String,
    val expected_examination_date: String?,
    val cmt: Double?,
    val drug_name: String?,
    val protocol_name: String?
)

@Serializable
data class CreatePostInjectionRequest(
    val examination_date: String?,
    val iop: Double?,
    @SerialName("centeral_macular_thickness") // Backend typo preserved
    val centralMacularThickness: Double?,
    val lens_status: String?,
    val notes: String?
)
