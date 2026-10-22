package com.example.drziadandroid.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    val id: String,
    val text: String,
    val author: String,
    val category: String = "Motivation",
    val arabicText: String? = null
)

@Serializable
data class WidgetPreferences(
    // Default Widget Preferences
    val defaultThemeStyle: String = "Dark",
    val defaultFontSize: Int = 16,
    val defaultTextAlignment: String = "Center",
    val selectedCategories: Set<String> = setOf("All"),
    val currentDefaultQuoteId: String? = null,

    // Islamic Widget Preferences
    val islamicThemeStyle: String = "Emerald",
    val islamicFontSize: Int = 18,
    val islamicTextAlignment: String = "Center",
    val currentIslamicQuoteId: String? = null,

    // Refresh & Automation Settings
    val refreshIntervalMillis: Long = 3600_000L, // 1 hour default (15m, 30m, 1h, 2h, 6h, 0L for manual)
    val refreshOnUnlock: Boolean = false          // Default OFF
)
