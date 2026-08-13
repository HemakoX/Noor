package com.example.drziadandroid.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.drziadandroid.data.model.*
import com.example.drziadandroid.presentation.components.*
import com.example.drziadandroid.presentation.viewmodel.PatientProfileUiState
import com.example.drziadandroid.presentation.viewmodel.PatientProfileViewModel
import com.example.drziadandroid.ui.theme.*
import java.util.UUID

// ─────────────────────────────────────────────────────────────────────────────
// Root Screen
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(
    code: String,
    viewModel: PatientProfileViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(code) { viewModel.loadProfile(code) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.profile?.name ?: "Patient Profile",
                            style = MaterialTheme.typography.titleLarge,
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.profile != null) {
                            Text(
                                text = uiState.profile!!.code,
                                style = MaterialTheme.typography.bodySmall,
                                color = SecondaryText
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = PrimaryText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        containerColor = Background
    ) { padding ->
        when {
            uiState.isLoading -> ProfileLoadingState(modifier = Modifier.padding(padding))
            uiState.error != null -> ProfileErrorState(
                error = uiState.error!!,
                onRetry = { viewModel.loadProfile(code) },
                modifier = Modifier.padding(padding)
            )
            uiState.profile != null -> ProfileContent(
                profile = uiState.profile!!,
                uiState = uiState,
                viewModel = viewModel,
                modifier = Modifier.padding(padding)
            )
            else -> ProfileLoadingState(modifier = Modifier.padding(padding))
        }
    }

    // ── Overlay forms ────────────────────────────────────────────────────────

    if (uiState.isEditingBasicInfo) {
        FullEditScreen(
            title = "Edit Patient",
            onDismiss = { viewModel.setEditingBasicInfo(false) }
        ) { onDismiss ->
            EditPatientForm(
                profile = uiState.profile,
                isSaving = uiState.isSavingBasicInfo,
                saveError = uiState.saveBasicInfoError,
                onSave = { name, phone, age, gender ->
                    viewModel.saveBasicInfo(name, phone, age, gender)
                },
                onCancel = onDismiss
            )
        }
    }

    if (uiState.isEditingMedicalHistory) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.setEditingMedicalHistory(false) },
            containerColor = SurfaceLight
        ) {
            EditMedicalHistorySheet(
                history = uiState.profile?.medical_history,
                isSaving = uiState.isSavingMedicalHistory,
                saveError = uiState.saveMedicalHistoryError,
                onSave = { d, h, hb -> viewModel.saveMedicalHistory(d, h, hb) },
                onCancel = { viewModel.setEditingMedicalHistory(false) }
            )
        }
    }

    if (uiState.isAddingBaseline) {
        FullEditScreen(
            title = "Add Baseline Assessment",
            onDismiss = { viewModel.setAddingBaseline(false) }
        ) { onDismiss ->
            AddBaselineForm(
                isSaving = uiState.isSavingBaseline,
                saveError = uiState.saveBaselineError,
                onSave = { viewModel.addBaseline(it) },
                onCancel = onDismiss
            )
        }
    }

    if (uiState.isAddingInjection) {
        FullEditScreen(
            title = "Record Injection",
            onDismiss = { viewModel.setAddingInjection(false) }
        ) { onDismiss ->
            AddInjectionForm(
                isSaving = uiState.isSavingInjection,
                saveError = uiState.saveInjectionError,
                onSave = { viewModel.addInjection(it) },
                onCancel = onDismiss
            )
        }
    }

    if (uiState.addingFollowUpToInjectionId != null) {
        FullEditScreen(
            title = "Post-Injection Follow-up",
            onDismiss = { viewModel.setAddingFollowUp(null) }
        ) { onDismiss ->
            AddFollowUpForm(
                injectionId = uiState.addingFollowUpToInjectionId!!,
                isSaving = uiState.isSavingInjection,
                saveError = uiState.saveInjectionError,
                onSave = { id, fu -> viewModel.addFollowUp(id, fu) },
                onCancel = onDismiss
            )
        }
    }

    if (uiState.isAddingDiagnosis) {
        AddDiagnosisSheet(
            onSave = { name, eye -> viewModel.addOcularDiagnosis(name, eye) },
            onDismiss = { viewModel.setAddingDiagnosis(false) }
        )
    }

    if (uiState.injectionPendingDeleteId != null) {
        ConfirmActionDialog(
            title = "Delete Injection?",
            message = "This injection record will be permanently removed. This action cannot be undone.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = { viewModel.confirmDeleteInjection() },
            onDismiss = { viewModel.cancelDeleteInjection() }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Loading / Error States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileLoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            CircularProgressIndicator(color = PrimaryAccent, strokeWidth = 3.dp)
            Text(
                "Loading patient profile…",
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
        }
    }
}

@Composable
private fun ProfileErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
            modifier = Modifier.padding(AppSpacing.extraLarge)
        ) {
            Icon(
                Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = StatusError,
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Could not load patient",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = SecondaryText
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text("Retry")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Main Profile Content
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProfileContent(
    profile: PatientProfileResponse,
    uiState: com.example.drziadandroid.presentation.viewmodel.PatientProfileUiState,
    viewModel: PatientProfileViewModel,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = AppSpacing.medium,
            end = AppSpacing.medium,
            top = AppSpacing.medium,
            bottom = AppSpacing.extraLarge
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        // 1. Patient Header
        item { PatientHeaderCard(profile = profile) }

        // 2. Patient Information
        item {
            ClinicalCard(
                title = "Patient Information",
                icon = Icons.Default.Person,
                action = { EditAction(onClick = { viewModel.setEditingBasicInfo(true) }) }
            ) {
                InfoRowMobile("Full Name", profile.name ?: "—")
                InfoRowMobile("Patient Code", profile.code)
                InfoRowMobile("Age", "${profile.age} years")
                InfoRowMobile("Gender", profile.gender.replaceFirstChar { it.uppercase() })
                InfoRowMobile("Phone", profile.phone ?: "—")
                if (!profile.date.isNullOrBlank()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = AppSpacing.small),
                        color = Border
                    )
                    InfoRowMobile("Registration Date", profile.date)
                    if (!profile.time.isNullOrBlank()) {
                        InfoRowMobile("Registration Time", profile.time)
                    }
                }
            }
        }

        // 3. Medical History
        item {
            ClinicalCard(
                title = "Medical History",
                icon = Icons.Default.History,
                action = { EditAction(onClick = { viewModel.setEditingMedicalHistory(true) }) }
            ) {
                val history = profile.medical_history
                if (history == null) {
                    SectionEmptyState(
                        message = "No medical history recorded",
                        icon = Icons.Default.History
                    )
                } else {
                    MedicalHistoryBadgeRow(
                        diabetes = history.diabetes,
                        hypertension = history.hypertension
                    )
                    if (history.diabetes) {
                        Spacer(modifier = Modifier.height(AppSpacing.small))
                        InfoRowMobile("HbA1c", history.hba1c?.let { "$it%" } ?: "—")
                    }
                }
            }
        }

        // 4. Baseline Eye Examination
        item {
            SectionHeader(
                title = "Baseline Eye Examination",
                icon = Icons.Default.RemoveRedEye,
                actionLabel = "Add",
                onAction = { viewModel.setAddingBaseline(true) }
            )
        }

        if (profile.base_eyes.isEmpty()) {
            item {
                SectionEmptyCard(
                    message = "No baseline assessments recorded",
                    actionLabel = "Add Baseline",
                    icon = Icons.Default.RemoveRedEye,
                    onAction = { viewModel.setAddingBaseline(true) }
                )
            }
        } else {
            itemsIndexed(profile.base_eyes) { _, eye ->
                BaselineEyeCard(eye = eye)
            }
        }

        // 5. Previous Eye History (shown inside baseline cards, but also summarized here)
        val eyesWithHistory = profile.base_eyes.filter { it.has_previous_history && it.previous_history != null }
        if (eyesWithHistory.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Previous Eye History",
                    icon = Icons.Default.AccessTime,
                    actionLabel = null,
                    onAction = {}
                )
            }
            itemsIndexed(eyesWithHistory) { _, eye ->
                PreviousHistoryCard(eye = eye, history = eye.previous_history!!)
            }
        }

        // 6. Injections
        item {
            SectionHeader(
                title = "Injections",
                icon = Icons.Default.MedicalServices,
                actionLabel = "Add",
                onAction = { viewModel.setAddingInjection(true) }
            )
        }

        if (profile.injections.isEmpty()) {
            item {
                SectionEmptyCard(
                    message = "No injections recorded",
                    actionLabel = "Record Injection",
                    icon = Icons.Default.MedicalServices,
                    onAction = { viewModel.setAddingInjection(true) }
                )
            }
        } else {
            itemsIndexed(profile.injections.asReversed()) { _, injection ->
                InjectionCard(
                    injection = injection,
                    onAddFollowUp = { viewModel.setAddingFollowUp(injection.id) },
                    onDelete = { viewModel.requestDeleteInjection(injection.id) }
                )
            }
        }

        // 7. Ocular Diagnoses
        item {
            ClinicalCard(
                title = "Ocular Diagnoses",
                icon = Icons.Default.Visibility,
                action = {
                    TextButton(
                        onClick = { viewModel.setAddingDiagnosis(true) },
                        contentPadding = PaddingValues(horizontal = AppSpacing.small, vertical = AppSpacing.extraSmall),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                        Text("Add", style = MaterialTheme.typography.labelLarge, color = PrimaryAccent)
                    }
                }
            ) {
                if (profile.ocular_diagnoses.isEmpty()) {
                    SectionEmptyState(
                        message = "No diagnoses assigned",
                        icon = Icons.Default.Visibility
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        profile.ocular_diagnoses.forEach { diagnosis ->
                            DiagnosisRow(diagnosis = diagnosis)
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Patient Header Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PatientHeaderCard(profile: PatientProfileResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = PrimaryAccent),
        shape = RoundedCornerShape(AppRadius.medium),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.low)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                val initials = profile.name
                    ?.split(" ")
                    ?.take(2)
                    ?.mapNotNull { it.firstOrNull()?.uppercase() }
                    ?.joinToString("") ?: "P"
                Text(
                    text = initials,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // Identity info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.name ?: "Unknown Patient",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = profile.code,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            // Quick stats column
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                HeaderBadge(
                    label = "${profile.age}y",
                    icon = Icons.Default.Cake
                )
                HeaderBadge(
                    label = profile.gender.replaceFirstChar { it.uppercase() },
                    icon = Icons.Default.Person
                )
                if (!profile.phone.isNullOrBlank()) {
                    HeaderBadge(
                        label = profile.phone,
                        icon = Icons.Default.Phone
                    )
                }
            }
        }
    }
}

@Composable
private fun HeaderBadge(label: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(12.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.9f))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Header (standalone, outside a card)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector,
    actionLabel: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = PrimaryAccent, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(AppSpacing.small))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryText,
                fontWeight = FontWeight.Bold
            )
        }
        if (actionLabel != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = AppSpacing.small, vertical = AppSpacing.extraSmall),
                modifier = Modifier.height(32.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                Text(actionLabel, style = MaterialTheme.typography.labelLarge, color = PrimaryAccent)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Section Empty States
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionEmptyState(message: String, icon: ImageVector) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Icon(icon, contentDescription = null, tint = Border, modifier = Modifier.size(32.dp))
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
    }
}

@Composable
private fun SectionEmptyCard(
    message: String,
    actionLabel: String,
    icon: ImageVector,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Icon(icon, contentDescription = null, tint = Border, modifier = Modifier.size(36.dp))
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
            OutlinedButton(
                onClick = onAction,
                border = BorderStroke(1.dp, PrimaryAccent),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
                Text(actionLabel)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Medical History
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MedicalHistoryBadgeRow(diabetes: Boolean, hypertension: Boolean) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small),
        modifier = Modifier.fillMaxWidth()
    ) {
        ConditionChip(label = "Diabetes", active = diabetes)
        ConditionChip(label = "Hypertension", active = hypertension)
    }
}

@Composable
private fun ConditionChip(label: String, active: Boolean) {
    val bgColor = if (active) StatusError.copy(alpha = 0.1f) else SurfaceVariantLight
    val textColor = if (active) StatusError else SecondaryText
    val borderColor = if (active) StatusError.copy(alpha = 0.4f) else Border

    Surface(
        shape = RoundedCornerShape(AppRadius.small),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.medium, vertical = AppSpacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                if (active) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = textColor)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Baseline Eye Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun BaselineEyeCard(eye: BaseEyeResponse) {
    val eyeLabel = when (eye.eye) {
        "OD" -> "Right Eye — OD"
        "OS" -> "Left Eye — OS"
        "OU" -> "Both Eyes — OU"
        else -> eye.eye ?: "Eye"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            // Eye label header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PrimaryAccent)
                )
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text(
                    text = eyeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = PrimaryAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(AppSpacing.medium))

            // Two-column layout for measurements
            val leftCol = listOfNotNull(
                "Visual Acuity" to (eye.visual_acuity ?: "—"),
                eye.va_snellen?.let { "Snellen" to it },
                eye.va_logmar?.let { "LogMAR" to it.toString() }
            )
            val rightCol = listOfNotNull(
                "IOP" to (eye.iop?.let { "$it mmHg" } ?: "—"),
                "Lens Status" to (eye.lens_status ?: "—"),
                "CMT" to (eye.central_macular_thickness?.let { "$it µm" } ?: "—")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)) {
                    leftCol.forEach { (label, value) ->
                        CompactMeasurement(label = label, value = value)
                    }
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)) {
                    rightCol.forEach { (label, value) ->
                        CompactMeasurement(label = label, value = value)
                    }
                }
            }

            if (!eye.primary_diagnosis.isNullOrBlank() || !eye.retinal_findings.isNullOrBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = AppSpacing.small), color = Border)
                if (!eye.primary_diagnosis.isNullOrBlank()) {
                    InfoRowMobile("Primary Diagnosis", eye.primary_diagnosis)
                }
                if (!eye.retinal_findings.isNullOrBlank()) {
                    InfoRowMobile("Retinal Findings", eye.retinal_findings)
                }
            }
        }
    }
}

@Composable
private fun CompactMeasurement(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = SecondaryText)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Previous Eye History Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PreviousHistoryCard(eye: BaseEyeResponse, history: PreviousHistoryResponse) {
    val eyeLabel = when (eye.eye) {
        "OD" -> "Right Eye (OD)"
        "OS" -> "Left Eye (OS)"
        "OU" -> "Both Eyes (OU)"
        else -> eye.eye ?: "Eye"
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, contentDescription = null, tint = SecondaryText, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text(
                    text = eyeLabel,
                    style = MaterialTheme.typography.titleSmall,
                    color = SecondaryText,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(AppSpacing.small))
            InfoRowMobile(
                "Previous Intravitreal Treatment",
                if (history.previous_intravitreal_treatment) "Yes" else "No"
            )
            if (history.previous_intravitreal_treatment) {
                InfoRowMobile(
                    "Number of Injections",
                    history.number_of_previous_injections?.toString() ?: "—"
                )
                InfoRowMobile("Date of Last Injection", history.date_of_last_injection ?: "—")
                InfoRowMobile("Previous Agent", history.previous_agent_name ?: "—")
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Injection Card
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InjectionCard(
    injection: InjectionResponse,
    onAddFollowUp: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val hasFollowUp = injection.post_injection != null
    val statusColor = if (hasFollowUp) StatusSuccess else StatusPending
    val statusIcon = if (hasFollowUp) Icons.Default.CheckCircle else Icons.Default.Schedule
    val statusText = if (hasFollowUp) "Followed up" else "Awaiting follow-up"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, if (expanded) PrimaryAccent.copy(alpha = 0.4f) else Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {

            // Header row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status indicator
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                        EyeTag(eye = injection.eye)
                        if (!injection.drug_name.isNullOrBlank()) {
                            Text(
                                text = injection.drug_name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = PrimaryText,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = injection.expected_examination_date ?: "Date not set",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryText
                    )
                }

                // Actions
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "Delete injection",
                        tint = StatusError.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = SecondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expanded detail
            if (expanded) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = AppSpacing.medium),
                    color = Border
                )

                // Injection details
                if (!injection.protocol_name.isNullOrBlank()) {
                    InfoRowMobile("Protocol", injection.protocol_name)
                }
                injection.cmt?.let { InfoRowMobile("Pre-injection CMT", "$it µm") }
                injection.visualActivity?.let { InfoRowMobile("Visual Acuity", it.toString()) }
                injection.va_snellen?.let { InfoRowMobile("Snellen", it) }
                injection.va_logmar?.let { InfoRowMobile("LogMAR", it.toString()) }

                if (injection.images.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.small))
                    Text(
                        "Pre-Injection Images",
                        style = MaterialTheme.typography.labelMedium,
                        color = SecondaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                    ImageHorizontalRow(injection.images)
                }

                // Post-injection follow-up
                if (!hasFollowUp) {
                    Spacer(modifier = Modifier.height(AppSpacing.small))
                    OutlinedButton(
                        onClick = onAddFollowUp,
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, PrimaryAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryAccent),
                        shape = RoundedCornerShape(AppRadius.small)
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(AppSpacing.small))
                        Text("Add Follow-up")
                    }
                } else {
                    Spacer(modifier = Modifier.height(AppSpacing.small))
                    PostInjectionSection(post = injection.post_injection!!)
                }
            } else {
                // Collapsed status line
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                    Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor)
                }
            }
        }
    }
}

@Composable
private fun EyeTag(eye: String?) {
    if (eye == null) return
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = PrimaryAccent.copy(alpha = 0.1f)
    ) {
        Text(
            text = eye,
            style = MaterialTheme.typography.labelSmall,
            color = PrimaryAccent,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PostInjectionSection(post: PostInjectionResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = StatusSuccess.copy(alpha = 0.05f),
        shape = RoundedCornerShape(AppRadius.small),
        border = BorderStroke(1.dp, StatusSuccess.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text("Post-Injection Follow-up", style = MaterialTheme.typography.labelLarge, color = StatusSuccess, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(AppSpacing.small))
            post.examination_date?.let { InfoRowMobile("Examination Date", it) }
            post.iop?.let { InfoRowMobile("IOP", "$it mmHg") }
            post.centralMacularThickness?.let { InfoRowMobile("Central Macular Thickness", "$it µm") }
            post.lens_status?.let { if (it.isNotBlank()) InfoRowMobile("Lens Status", it) }
            post.visualActivity?.let { InfoRowMobile("Visual Acuity", it.toString()) }
            post.va_snellen?.let { InfoRowMobile("Snellen", it) }
            if (!post.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(AppSpacing.extraSmall))
                Text("Notes", style = MaterialTheme.typography.labelSmall, color = SecondaryText)
                Text(post.notes, style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
            }
            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AppSpacing.small))
                Text("Post-Injection Images", style = MaterialTheme.typography.labelSmall, color = SecondaryText, fontWeight = FontWeight.SemiBold)
                ImageHorizontalRow(post.images)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Ocular Diagnoses
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiagnosisRow(diagnosis: OcularDiagnosisResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        Icon(
            Icons.Default.FiberManualRecord,
            contentDescription = null,
            tint = PrimaryAccent,
            modifier = Modifier.size(8.dp)
        )
        Text(
            text = diagnosis.primary_diagnosis_name,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        diagnosis.eye?.let { eye ->
            EyeTag(eye = eye)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Image Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ImageHorizontalRow(images: List<ImageResponse>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = AppSpacing.small),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
    ) {
        images.forEach { _ ->
            Card(
                modifier = Modifier.size(72.dp),
                shape = RoundedCornerShape(AppRadius.small),
                border = BorderStroke(1.dp, Border)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = Border, modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Patient Form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditPatientForm(
    profile: PatientProfileResponse?,
    isSaving: Boolean,
    saveError: String?,
    onSave: (String, String, Int, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var phone by remember { mutableStateOf(profile?.phone ?: "") }
    var ageText by remember { mutableStateOf(profile?.age?.toString() ?: "") }
    var gender by remember { mutableStateOf(profile?.gender ?: "male") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var ageError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            "Personal Details",
            style = MaterialTheme.typography.titleMedium,
            color = PrimaryAccent,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameError = null },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth(),
            isError = nameError != null,
            supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true
        )

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true
        )

        OutlinedTextField(
            value = ageText,
            onValueChange = { ageText = it; ageError = null },
            label = { Text("Age") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = ageError != null,
            supportingText = ageError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            singleLine = true
        )

        Column {
            Text("Gender", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
            ) {
                listOf("male" to "Male", "female" to "Female").forEach { (value, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { gender = value }
                    ) {
                        RadioButton(
                            selected = gender == value,
                            onClick = { gender = value },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryAccent)
                        )
                        Text(label, style = MaterialTheme.typography.bodyMedium, color = PrimaryText)
                    }
                }
            }
        }

        // Save error
        if (saveError != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = StatusError.copy(alpha = 0.1f),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                Row(
                    modifier = Modifier.padding(AppSpacing.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)
                ) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = StatusError, modifier = Modifier.size(16.dp))
                    Text(saveError, style = MaterialTheme.typography.bodySmall, color = StatusError)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                enabled = !isSaving
            ) { Text("Cancel") }

            Button(
                onClick = {
                    nameError = null; ageError = null
                    val age = ageText.toIntOrNull()
                    var valid = true
                    if (name.isBlank()) { nameError = "Name is required"; valid = false }
                    if (age == null || age <= 0) { ageError = "Enter a valid age"; valid = false }
                    if (valid) onSave(name.trim(), phone.trim(), age!!, gender)
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Edit Medical History Sheet
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun EditMedicalHistorySheet(
    history: MedicalHistoryResponse?,
    isSaving: Boolean,
    saveError: String?,
    onSave: (Boolean, Boolean, Double?) -> Unit,
    onCancel: () -> Unit
) {
    var diabetes by remember { mutableStateOf(history?.diabetes ?: false) }
    var hypertension by remember { mutableStateOf(history?.hypertension ?: false) }
    var hba1c by remember { mutableStateOf(history?.hba1c?.toString() ?: "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.large)
            .padding(bottom = AppSpacing.extraLarge)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text(
            "Edit Medical History",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = PrimaryText
        )

        HorizontalDivider(color = Border)

        SwitchRow(label = "Diabetes", checked = diabetes, onCheckedChange = { diabetes = it })
        SwitchRow(label = "Hypertension", checked = hypertension, onCheckedChange = { hypertension = it })

        if (diabetes) {
            OutlinedTextField(
                value = hba1c,
                onValueChange = { hba1c = it },
                label = { Text("HbA1c (%)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        if (saveError != null) {
            Text(saveError, style = MaterialTheme.typography.bodySmall, color = StatusError)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            TextButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) {
                Text("Cancel")
            }
            Button(
                onClick = { onSave(diabetes, hypertension, hba1c.toDoubleOrNull()) },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = PrimaryText)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedTrackColor = PrimaryAccent)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Baseline Form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddBaselineForm(
    isSaving: Boolean,
    saveError: String?,
    onSave: (BaseEyeResponse) -> Unit,
    onCancel: () -> Unit
) {
    var eye by remember { mutableStateOf("OD") }
    var va by remember { mutableStateOf("") }
    var vaSnellen by remember { mutableStateOf("") }
    var vaLogmar by remember { mutableStateOf("") }
    var iop by remember { mutableStateOf("") }
    var lensStatus by remember { mutableStateOf("") }
    var cmt by remember { mutableStateOf("") }
    var primaryDiagnosis by remember { mutableStateOf("") }
    var retinalFindings by remember { mutableStateOf("") }
    var hasPrevHistory by remember { mutableStateOf(false) }
    var prevTreatment by remember { mutableStateOf(false) }
    var prevInjectionCount by remember { mutableStateOf("") }
    var dateLastInjection by remember { mutableStateOf("") }
    var prevAgentName by remember { mutableStateOf("") }

    var showLensPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        // Eye selection
        Text("Eye", style = MaterialTheme.typography.titleSmall, color = SecondaryText)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            listOf("OD" to "Right (OD)", "OS" to "Left (OS)", "OU" to "Both (OU)").forEach { (value, label) ->
                FilterChip(
                    selected = eye == value,
                    onClick = { eye = value },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryAccent, selectedLabelColor = Color.White)
                )
            }
        }

        HorizontalDivider(color = Border)

        Text("Visual Measurements", style = MaterialTheme.typography.titleSmall, color = SecondaryText)
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            OutlinedTextField(
                value = va,
                onValueChange = { va = it },
                label = { Text("Visual Acuity") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = iop,
                onValueChange = { iop = it },
                label = { Text("IOP (mmHg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            OutlinedTextField(
                value = vaSnellen,
                onValueChange = { vaSnellen = it },
                label = { Text("Snellen") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = vaLogmar,
                onValueChange = { vaLogmar = it },
                label = { Text("LogMAR") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }
        OutlinedTextField(
            value = cmt,
            onValueChange = { cmt = it },
            label = { Text("Central Macular Thickness (µm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        HorizontalDivider(color = Border)

        Text("Clinical Information", style = MaterialTheme.typography.titleSmall, color = SecondaryText)
        DynamicSelectionField(
            label = "Lens Status",
            value = lensStatus,
            onClick = { showLensPicker = true }
        )
        OutlinedTextField(
            value = primaryDiagnosis,
            onValueChange = { primaryDiagnosis = it },
            label = { Text("Primary Diagnosis") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = retinalFindings,
            onValueChange = { retinalFindings = it },
            label = { Text("Retinal Findings") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        HorizontalDivider(color = Border)

        // Previous history toggle
        SwitchRow(
            label = "Has Previous Intravitreal History",
            checked = hasPrevHistory,
            onCheckedChange = { hasPrevHistory = it }
        )

        if (hasPrevHistory) {
            SwitchRow(
                label = "Previous Intravitreal Treatment",
                checked = prevTreatment,
                onCheckedChange = { prevTreatment = it }
            )
            if (prevTreatment) {
                OutlinedTextField(
                    value = prevInjectionCount,
                    onValueChange = { prevInjectionCount = it },
                    label = { Text("Number of Previous Injections") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = dateLastInjection,
                    onValueChange = { dateLastInjection = it },
                    label = { Text("Date of Last Injection (YYYY-MM-DD)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = prevAgentName,
                    onValueChange = { prevAgentName = it },
                    label = { Text("Previous Agent Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        if (saveError != null) {
            Text(saveError, style = MaterialTheme.typography.bodySmall, color = StatusError)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) { Text("Cancel") }
            Button(
                onClick = {
                    val prevHistory = if (hasPrevHistory) PreviousHistoryResponse(
                        previous_intravitreal_treatment = prevTreatment,
                        number_of_previous_injections = prevInjectionCount.toIntOrNull(),
                        date_of_last_injection = dateLastInjection.takeIf { it.isNotBlank() },
                        previous_agent_name = prevAgentName.takeIf { it.isNotBlank() }
                    ) else null

                    onSave(
                        BaseEyeResponse(
                            id = UUID.randomUUID().toString(),
                            eye = eye,
                            visual_acuity = va.takeIf { it.isNotBlank() },
                            va_snellen = vaSnellen.takeIf { it.isNotBlank() },
                            va_logmar = vaLogmar.toDoubleOrNull(),
                            iop = iop.toDoubleOrNull(),
                            lens_status = lensStatus.takeIf { it.isNotBlank() },
                            central_macular_thickness = cmt.toDoubleOrNull(),
                            primary_diagnosis = primaryDiagnosis.takeIf { it.isNotBlank() },
                            retinal_findings = retinalFindings.takeIf { it.isNotBlank() },
                            has_previous_history = hasPrevHistory,
                            previous_history = prevHistory
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Add Baseline")
            }
        }
    }

    if (showLensPicker) {
        LensStatusBottomSheet(
            currentValue = lensStatus,
            onSelect = { lensStatus = it },
            onDismiss = { showLensPicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Injection Form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddInjectionForm(
    isSaving: Boolean,
    saveError: String?,
    onSave: (InjectionResponse) -> Unit,
    onCancel: () -> Unit
) {
    var eye by remember { mutableStateOf("OD") }
    var drug by remember { mutableStateOf("") }
    var protocol by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cmt by remember { mutableStateOf("") }
    var va by remember { mutableStateOf("") }

    var showDrugPicker by remember { mutableStateOf(false) }
    var showProtocolPicker by remember { mutableStateOf(false) }

    // Local catalogs — empty by default. User types or adds new.
    var drugs by remember { mutableStateOf(emptyList<String>()) }
    var protocols by remember { mutableStateOf(emptyList<String>()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text("Injection Details", style = MaterialTheme.typography.titleSmall, color = SecondaryText)

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            listOf("OD" to "Right (OD)", "OS" to "Left (OS)").forEach { (value, label) ->
                FilterChip(
                    selected = eye == value,
                    onClick = { eye = value },
                    label = { Text(label) },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = PrimaryAccent, selectedLabelColor = Color.White)
                )
            }
        }

        DynamicSelectionField("Drug", drug, onClick = { showDrugPicker = true })
        DynamicSelectionField("Protocol", protocol, onClick = { showProtocolPicker = true })

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Expected Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            OutlinedTextField(
                value = cmt,
                onValueChange = { cmt = it },
                label = { Text("Pre-Inj. CMT (µm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = va,
                onValueChange = { va = it },
                label = { Text("Visual Acuity") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        if (saveError != null) {
            Text(saveError, style = MaterialTheme.typography.bodySmall, color = StatusError)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) { Text("Cancel") }
            Button(
                onClick = {
                    onSave(
                        InjectionResponse(
                            id = UUID.randomUUID().toString(),
                            eye = eye,
                            expected_examination_date = date.takeIf { it.isNotBlank() },
                            cmt = cmt.toDoubleOrNull(),
                            drug_name = drug.takeIf { it.isNotBlank() },
                            protocol_name = protocol.takeIf { it.isNotBlank() }
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Record Injection")
            }
        }
    }

    if (showDrugPicker) {
        DynamicSelectionBottomSheet(
            title = "Select Drug",
            items = drugs,
            itemDisplayName = { it },
            onItemSelected = { drug = it },
            onAddNew = { name -> drugs = drugs + name },
            onDismiss = { showDrugPicker = false }
        )
    }
    if (showProtocolPicker) {
        DynamicSelectionBottomSheet(
            title = "Select Protocol",
            items = protocols,
            itemDisplayName = { it },
            onItemSelected = { protocol = it },
            onAddNew = { name -> protocols = protocols + name },
            onDismiss = { showProtocolPicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Follow-up Form
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AddFollowUpForm(
    injectionId: String,
    isSaving: Boolean,
    saveError: String?,
    onSave: (String, PostInjectionResponse) -> Unit,
    onCancel: () -> Unit
) {
    var date by remember { mutableStateOf("") }
    var iop by remember { mutableStateOf("") }
    var cmt by remember { mutableStateOf("") }
    var lensStatus by remember { mutableStateOf("") }
    var va by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var showLensPicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(AppSpacing.large)
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
    ) {
        Text("Follow-up Details", style = MaterialTheme.typography.titleSmall, color = SecondaryText)

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Examination Date (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
            OutlinedTextField(
                value = iop,
                onValueChange = { iop = it },
                label = { Text("IOP (mmHg)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            OutlinedTextField(
                value = cmt,
                onValueChange = { cmt = it },
                label = { Text("CMT (µm)") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = va,
            onValueChange = { va = it },
            label = { Text("Visual Acuity") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        DynamicSelectionField("Lens Status", lensStatus, onClick = { showLensPicker = true })

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Clinical Notes") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 100.dp),
            minLines = 3,
            maxLines = 6
        )

        if (saveError != null) {
            Text(saveError, style = MaterialTheme.typography.bodySmall, color = StatusError)
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f), enabled = !isSaving) { Text("Cancel") }
            Button(
                onClick = {
                    onSave(
                        injectionId,
                        PostInjectionResponse(
                            id = UUID.randomUUID().toString(),
                            examination_date = date.takeIf { it.isNotBlank() },
                            iop = iop.toDoubleOrNull(),
                            centralMacularThickness = cmt.toDoubleOrNull(),
                            lens_status = lensStatus.takeIf { it.isNotBlank() },
                            notes = notes.takeIf { it.isNotBlank() }
                        )
                    )
                },
                modifier = Modifier.weight(1f),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                shape = RoundedCornerShape(AppRadius.small)
            ) {
                if (isSaving) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                else Text("Save Follow-up")
            }
        }
    }

    if (showLensPicker) {
        LensStatusBottomSheet(
            currentValue = lensStatus,
            onSelect = { lensStatus = it },
            onDismiss = { showLensPicker = false }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Add Diagnosis Sheet
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDiagnosisSheet(
    onSave: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var diagnosisName by remember { mutableStateOf("") }
    var eye by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.large)
                .padding(bottom = AppSpacing.extraLarge)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)
        ) {
            Text(
                "Add Ocular Diagnosis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            HorizontalDivider(color = Border)

            OutlinedTextField(
                value = diagnosisName,
                onValueChange = { diagnosisName = it; nameError = null },
                label = { Text("Diagnosis Name") },
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                singleLine = true
            )

            Text("Eye (optional)", style = MaterialTheme.typography.bodySmall, color = SecondaryText)
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.small)) {
                listOf<String?>(null, "OD", "OS", "OU").forEach { value ->
                    FilterChip(
                        selected = eye == value,
                        onClick = { eye = value },
                        label = { Text(value ?: "Both") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryAccent,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
            ) {
                TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                Button(
                    onClick = {
                        if (diagnosisName.isBlank()) {
                            nameError = "Diagnosis name is required"
                        } else {
                            onSave(diagnosisName.trim(), eye)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(AppRadius.small)
                ) { Text("Add Diagnosis") }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Lens Status Bottom Sheet (reusable local picker for lens status values)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun LensStatusBottomSheet(
    currentValue: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Standard ophthalmology lens status values — this is domain terminology, not catalog data
    val standardValues = listOf("Phakic", "Pseudophakic", "Aphakic", "Cataract")
    DynamicSelectionBottomSheet(
        title = "Lens Status",
        items = standardValues,
        itemDisplayName = { it },
        onItemSelected = { onSelect(it) },
        onAddNew = { newValue -> onSelect(newValue) },
        onDismiss = onDismiss
    )
}
