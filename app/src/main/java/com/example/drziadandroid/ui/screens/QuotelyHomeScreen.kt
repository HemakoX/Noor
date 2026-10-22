package com.example.drziadandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.drziadandroid.ui.viewmodel.QuotelyViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuotelyHomeScreen(viewModel: QuotelyViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Noor",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                actions = {
                    // Online/Offline Status Indicator
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (uiState.isOnline) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (uiState.isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isOnline) "Online" else "Offline Cache",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (uiState.isOnline) Color(0xFF10B981) else Color(0xFFD97706)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Widget Selector Tabs
            TabRow(
                selectedTabIndex = uiState.activeTab,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.setActiveTab(0) },
                    text = { Text("Default Widget", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.setActiveTab(1) },
                    text = { Text("🕌 Islamic Widget", fontWeight = FontWeight.Bold) }
                )
            }

            if (uiState.activeTab == 0) {
                // DEFAULT WIDGET TAB
                Text(
                    text = "Default Quotes Widget Settings",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val defaultQuote = uiState.currentDefaultQuote

                LiveWidgetPreviewCard(
                    quote = defaultQuote?.text ?: "The journey of a thousand miles begins with one step.",
                    author = defaultQuote?.author ?: "Lao Tzu",
                    category = defaultQuote?.category ?: "Motivation",
                    arabicText = null,
                    themeStyle = uiState.preferences.defaultThemeStyle,
                    fontSizeSp = uiState.preferences.defaultFontSize,
                    alignment = uiState.preferences.defaultTextAlignment,
                    onRefreshClicked = { viewModel.cycleNextDefaultQuote() }
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.cycleNextDefaultQuote() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next Quote")
                    }

                    OutlinedButton(
                        onClick = { viewModel.fetchDefaultQuotes() },
                        enabled = !uiState.isFetching,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fetch 10 Quotes")
                        }
                    }
                }

                // Default Customization Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Widget Customization", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Text(text = "Background Theme", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val themes = listOf("Dark", "Light", "Sunset", "Midnight", "Emerald", "Glass")
                            themes.forEach { theme ->
                                val selected = uiState.preferences.defaultThemeStyle == theme
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateDefaultTheme(theme) },
                                    label = { Text(theme) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text(text = "Font Size", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val sizes = listOf(14 to "Small", 16 to "Medium", 20 to "Large", 24 to "X-Large")
                            sizes.forEach { (size, label) ->
                                val selected = uiState.preferences.defaultFontSize == size
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateDefaultFontSize(size) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text(text = "Text Alignment", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val alignments = listOf(
                                "Left" to Icons.Default.FormatAlignLeft,
                                "Center" to Icons.Default.FormatAlignCenter,
                                "Right" to Icons.Default.FormatAlignRight
                            )
                            alignments.forEach { (align, icon) ->
                                val selected = uiState.preferences.defaultTextAlignment == align
                                IconButton(
                                    onClick = { viewModel.updateDefaultAlignment(align) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 0.dp,
                                            color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = align,
                                        tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Default Categories Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Preferred Categories", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf("All", "Motivation", "Philosophy", "Science", "Life", "Wisdom", "Success", "Humor", "Art", "Tech")
                            categories.forEach { category ->
                                val selected = uiState.preferences.selectedCategories.contains(category)
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.toggleCategory(category) },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            } else {
                // ISLAMIC WIDGET TAB
                Text(
                    text = "Islamic Widget Settings (آيات قرآنية وأحاديث)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                val islamicQuote = uiState.currentIslamicQuote

                LiveWidgetPreviewCard(
                    quote = "",
                    author = islamicQuote?.author ?: "سورة البقرة [2:152]",
                    category = islamicQuote?.category ?: "Quran",
                    arabicText = islamicQuote?.arabicText ?: "فَاذْكُرُونِي أَذْكُرْكُمْ",
                    themeStyle = uiState.preferences.islamicThemeStyle,
                    fontSizeSp = uiState.preferences.islamicFontSize,
                    alignment = uiState.preferences.islamicTextAlignment,
                    onRefreshClicked = { viewModel.cycleNextIslamicQuote() }
                )

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.cycleNextIslamicQuote() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next Verse / Hadith")
                    }

                    OutlinedButton(
                        onClick = { viewModel.fetchIslamicQuotes() },
                        enabled = !uiState.isFetching,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isFetching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Fetch Islamic Batch")
                        }
                    }
                }

                // Islamic Customization Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(text = "Islamic Widget Appearance", fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Text(text = "Background Theme", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val themes = listOf("Emerald", "Dark", "Light", "Sunset", "Midnight", "Glass")
                            themes.forEach { theme ->
                                val selected = uiState.preferences.islamicThemeStyle == theme
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateIslamicTheme(theme) },
                                    label = { Text(theme) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text(text = "Font Size", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val sizes = listOf(14 to "Small", 16 to "Medium", 18 to "Large", 22 to "X-Large")
                            sizes.forEach { (size, label) ->
                                val selected = uiState.preferences.islamicFontSize == size
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.updateIslamicFontSize(size) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF059669),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        Text(text = "Text Alignment", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val alignments = listOf(
                                "Left" to Icons.Default.FormatAlignLeft,
                                "Center" to Icons.Default.FormatAlignCenter,
                                "Right" to Icons.Default.FormatAlignRight
                            )
                            alignments.forEach { (align, icon) ->
                                val selected = uiState.preferences.islamicTextAlignment == align
                                IconButton(
                                    onClick = { viewModel.updateIslamicAlignment(align) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) Color(0xFF059669).copy(alpha = 0.2f)
                                            else MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = if (selected) 2.dp else 0.dp,
                                            color = if (selected) Color(0xFF059669) else Color.Transparent,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = align,
                                        tint = if (selected) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 4: Offline Cache Info
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Independent Offline Widgets",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Default Quotes Cache: ${uiState.defaultQuotesCount} items | Islamic Cache: ${uiState.islamicQuotesCount} items. Place either widget or both on your home screen!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Section 5: Widget Setup Guide
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Widgets,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Adding Both Widgets to Home Screen",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Text(text = "1. Long press on your Android Home Screen.", fontSize = 13.sp)
                    Text(text = "2. Select 'Widgets' and search for 'Noor'.", fontSize = 13.sp)
                    Text(text = "3. You will see two separate widgets: 'Noor - Default Quotes' and 'Noor - Islamic (آيات وأحاديث)'.", fontSize = 13.sp)
                    Text(text = "4. Drag either or both widgets onto your home screen!", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LiveWidgetPreviewCard(
    quote: String,
    author: String,
    category: String,
    arabicText: String? = null,
    themeStyle: String,
    fontSizeSp: Int,
    alignment: String,
    onRefreshClicked: () -> Unit
) {
    val isLight = themeStyle == "Light"
    val textColor = if (isLight) Color(0xFF1E293B) else Color.White
    val authorColor = if (isLight) Color(0xFF475569) else Color(0xFFCBD5E0)
    val badgeColor = if (isLight) Color(0xFF64748B) else Color(0xFFA0AEC0)

    val isIslamic = category == "Quran" || category == "Hadith" || category == "Islamic" || !arabicText.isNullOrBlank()

    val textAlign = when (alignment) {
        "Left" -> TextAlign.Start
        "Right" -> TextAlign.End
        else -> TextAlign.Center
    }

    val backgroundModifier = when (themeStyle) {
        "Light" -> Modifier.background(Color(0xFFF8F9FA))
        "Sunset" -> Modifier.background(
            Brush.linearGradient(listOf(Color(0xFF1E1B4B), Color(0xFF6B21A8)))
        )
        "Midnight" -> Modifier.background(
            Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
        )
        "Emerald" -> Modifier.background(
            Brush.linearGradient(listOf(Color(0xFF022C22), Color(0xFF064E3B)))
        )
        "Glass" -> Modifier.background(Color(0xE6111827))
        else -> Modifier.background(Color(0xFF1A1C23))
    }

    val displayAuthor = if (author.contains(" •")) author.substringBefore(" •") else author

    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(210.dp)
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(backgroundModifier)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val badgeLabel = when (category) {
                        "Quran" -> "آية قرآنية"
                        "Hadith" -> "حديث شريف"
                        else -> category.uppercase()
                    }

                    Text(
                        text = "NOOR • $badgeLabel",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                    IconButton(
                        onClick = onRefreshClicked,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = badgeColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Center Quote Text (RTL composition if Islamic text present)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = when (alignment) {
                            "Left" -> Alignment.Start
                            "Right" -> Alignment.End
                            else -> Alignment.CenterHorizontally
                        }
                    ) {
                        if (isIslamic) {
                            val mainText = if (!arabicText.isNullOrBlank()) arabicText else quote
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                                Text(
                                    text = mainText,
                                    fontSize = (fontSizeSp + 3).sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor,
                                    textAlign = textAlign,
                                    style = TextStyle(
                                        textDirection = TextDirection.Rtl
                                    ),
                                    lineHeight = (fontSizeSp + 9).sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        } else {
                            Text(
                                text = "\"$quote\"",
                                fontSize = fontSizeSp.sp,
                                fontWeight = FontWeight.Medium,
                                color = textColor,
                                textAlign = textAlign,
                                lineHeight = (fontSizeSp + 5).sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Author Text
                Text(
                    text = "— $displayAuthor",
                    fontSize = 13.sp,
                    fontStyle = FontStyle.Italic,
                    color = authorColor,
                    textAlign = textAlign,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
