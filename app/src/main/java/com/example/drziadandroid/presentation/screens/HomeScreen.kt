package com.example.drziadandroid.presentation.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.drziadandroid.presentation.components.ClinicalCard
import com.example.drziadandroid.presentation.components.StatCard
import com.example.drziadandroid.presentation.viewmodel.DashboardViewModel
import com.example.drziadandroid.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit,
    onPatientsClick: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Dr. Ziad Clinic", 
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    ) 
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout", tint = StatusError)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceLight)
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.large)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search patient by name or code...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceLight,
                    unfocusedContainerColor = SurfaceLight,
                    focusedBorderColor = PrimaryAccent
                )
            )

            // Statistics Grid
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(
                    text = "Overview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )
                
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.medium),
                    maxItemsInEachRow = 2
                ) {
                    val itemModifier = Modifier.weight(1f).minWidth(150.dp)
                    
                    StatCard(
                        label = "Total Patients",
                        value = uiState.totalPatients.toString(),
                        icon = Icons.Default.People,
                        color = PrimaryAccent,
                        modifier = itemModifier
                    )
                    StatCard(
                        label = "Today's Visits",
                        value = uiState.todayPatients.size.toString(),
                        icon = Icons.Default.Event,
                        color = StatusInfo,
                        modifier = itemModifier
                    )
                    StatCard(
                        label = "Pending",
                        value = uiState.pendingAppointments.toString(),
                        icon = Icons.Default.Schedule,
                        color = StatusPending,
                        modifier = itemModifier
                    )
                    StatCard(
                        label = "Monthly Growth",
                        value = "+${uiState.newPatientsThisMonth}",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = StatusSuccess,
                        modifier = itemModifier
                    )
                }
            }

            // Quick Actions
            Button(
                onClick = onPatientsClick,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                Spacer(modifier = Modifier.width(AppSpacing.small))
                Text("View All Patients")
            }

            // Today's Appointments
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                Text(
                    text = "Today's Appointments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryText
                )

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryAccent)
                    }
                } else if (uiState.error != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = StatusError.copy(alpha = 0.1f))
                    ) {
                        Text(
                            text = uiState.error!!,
                            modifier = Modifier.padding(AppSpacing.medium),
                            color = StatusError
                        )
                    }
                } else if (uiState.todayPatients.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Border),
                        colors = CardDefaults.cardColors(containerColor = SurfaceLight)
                    ) {
                        Box(modifier = Modifier.padding(AppSpacing.large).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No appointments scheduled for today", color = SecondaryText)
                        }
                    }
                } else {
                    uiState.todayPatients.forEach { patient ->
                        ClinicalCard(
                            title = patient.name ?: "Unknown",
                            icon = Icons.Default.Person,
                            modifier = Modifier.clickable { onPatientClick(patient.code) },
                            action = {
                                Text(
                                    text = patient.time ?: "--:--",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = PrimaryAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Code: ${patient.code}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                                Text(
                                    text = "${patient.age} years • ${patient.gender}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SecondaryText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(AppSpacing.small))
                    }
                }
            }
        }
    }
}

private fun Modifier.minWidth(width: Dp): Modifier = this.then(Modifier.widthIn(min = width))
