package com.example.drziadandroid.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.drziadandroid.ui.theme.*

@Composable
fun ClinicalCard(
    title: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.none),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(modifier = Modifier.padding(AppSpacing.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = PrimaryAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.small))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = PrimaryText,
                        fontWeight = FontWeight.Bold
                    )
                }
                action?.invoke()
            }
            Spacer(modifier = Modifier.height(AppSpacing.medium))
            content()
        }
    }
}

@Composable
fun EditAction(onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = AppSpacing.small, vertical = AppSpacing.extraSmall),
        modifier = Modifier.height(32.dp)
    ) {
        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(AppSpacing.extraSmall))
        Text("Edit", style = MaterialTheme.typography.labelLarge, color = PrimaryAccent)
    }
}

@Composable
fun InfoRowMobile(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = SecondaryText)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = PrimaryText,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EmptyStateCard(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = message, color = SecondaryText)
        }
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceLight),
        border = BorderStroke(1.dp, Border),
        shape = RoundedCornerShape(AppRadius.medium)
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.extraSmall)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = SecondaryText
            )
        }
    }
}

@Composable
fun ConfirmActionDialog(
    title: String,
    message: String,
    confirmText: String = "Delete",
    isDestructive: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDestructive) StatusError else PrimaryAccent
                )
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FullEditScreen(
    title: String,
    onDismiss: () -> Unit,
    content: @Composable (onCancel: () -> Unit) -> Unit
) {
    var showDiscardConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = !showDiscardConfirm) {
        showDiscardConfirm = true
    }

    Dialog(
        onDismissRequest = { showDiscardConfirm = true },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Surface(
                    color = SurfaceLight,
                    shadowElevation = AppElevation.low
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                            .padding(horizontal = AppSpacing.extraSmall),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { showDiscardConfirm = true }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f),
                            color = PrimaryText,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    content { showDiscardConfirm = true }
                }
            }
        }
    }

    if (showDiscardConfirm) {
        ConfirmActionDialog(
            title = "Discard changes?",
            message = "Your changes haven't been saved.",
            confirmText = "Discard",
            isDestructive = true,
            onConfirm = {
                showDiscardConfirm = false
                onDismiss()
            },
            onDismiss = { showDiscardConfirm = false }
        )
    }
}

@Composable
fun DynamicSelectionField(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.clickable(onClick = onClick)) {
        OutlinedTextField(
            value = if (value.isEmpty()) "Select..." else value,
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            enabled = false,
            colors = OutlinedTextFieldDefaults.colors(
                disabledTextColor = if (value.isEmpty()) SecondaryText else PrimaryText,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = SecondaryText
            ),
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DynamicSelectionBottomSheet(
    title: String,
    items: List<T>,
    itemDisplayName: (T) -> String,
    onItemSelected: (T) -> Unit,
    onAddNew: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var isAddingNew by remember { mutableStateOf(false) }
    var newValue by remember { mutableStateOf("") }

    val filteredItems = items.filter { itemDisplayName(it).contains(searchQuery, ignoreCase = true) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = SurfaceLight,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.extraLarge)
        ) {
            Text(
                text = if (isAddingNew) "Add New" else title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(AppSpacing.medium),
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            if (!isAddingNew) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppSpacing.medium),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(AppRadius.medium)
                )

                Spacer(modifier = Modifier.height(AppSpacing.small))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp)
                ) {
                    if (filteredItems.isEmpty() && searchQuery.isNotBlank() && onAddNew != null) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text("+ Add \"$searchQuery\"", color = PrimaryAccent, fontWeight = FontWeight.Bold)
                                },
                                modifier = Modifier.clickable {
                                    newValue = searchQuery
                                    isAddingNew = true
                                }
                            )
                        }
                    }

                    items(filteredItems) { item ->
                        ListItem(
                            headlineContent = { Text(itemDisplayName(item)) },
                            modifier = Modifier.clickable {
                                onItemSelected(item)
                                onDismiss()
                            }
                        )
                    }

                    if (onAddNew != null && !filteredItems.any { itemDisplayName(it).equals(searchQuery, true) }) {
                        item {
                            ListItem(
                                headlineContent = {
                                    Text("+ Add New", color = PrimaryAccent, fontWeight = FontWeight.Bold)
                                },
                                modifier = Modifier.clickable { isAddingNew = true }
                            )
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(AppSpacing.large), verticalArrangement = Arrangement.spacedBy(AppSpacing.medium)) {
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.medium)
                    ) {
                        TextButton(onClick = { isAddingNew = false }, modifier = Modifier.weight(1f)) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (newValue.isNotBlank()) {
                                    onAddNew?.invoke(newValue)
                                    // In a real app we'd wait for backend response but here we just mock
                                    @Suppress("UNCHECKED_CAST")
                                    onItemSelected(newValue as T)
                                    onDismiss()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent)
                        ) {
                            Text("Add & Select")
                        }
                    }
                }
            }
        }
    }
}
