package com.example.drziadandroid.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.drziadandroid.data.model.Quote
import com.example.drziadandroid.data.model.WidgetPreferences
import com.example.drziadandroid.data.repository.QuoteRepository
import com.example.drziadandroid.widget.DefaultWidgetProvider
import com.example.drziadandroid.widget.IslamicWidgetProvider
import com.example.drziadandroid.widget.WidgetScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuotelyUiState(
    val currentDefaultQuote: Quote? = null,
    val currentIslamicQuote: Quote? = null,
    val preferences: WidgetPreferences = WidgetPreferences(),
    val defaultQuotesCount: Int = 0,
    val islamicQuotesCount: Int = 0,
    val activeTab: Int = 0, // 0 = Default Widget, 1 = Islamic Widget
    val isFetching: Boolean = false,
    val userMessage: String? = null,
    val isOnline: Boolean = true
)

class QuotelyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = QuoteRepository(application)

    private val _uiState = MutableStateFlow(QuotelyUiState())
    val uiState: StateFlow<QuotelyUiState> = _uiState.asStateFlow()

    init {
        refreshState()
    }

    fun refreshState() {
        viewModelScope.launch {
            val defaultQuote = repository.getCurrentDefaultQuote()
            val islamicQuote = repository.getCurrentIslamicQuote()
            val prefs = repository.getPreferences()
            val defaultList = repository.getFilteredDefaultQuotes()
            val islamicList = repository.getIslamicQuotes()
            val online = checkOnlineStatus()

            _uiState.update {
                it.copy(
                    currentDefaultQuote = defaultQuote,
                    currentIslamicQuote = islamicQuote,
                    preferences = prefs,
                    defaultQuotesCount = defaultList.size,
                    islamicQuotesCount = islamicList.size,
                    isOnline = online
                )
            }
        }
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.update { it.copy(activeTab = tabIndex) }
    }

    // Default Widget Actions
    fun cycleNextDefaultQuote() {
        viewModelScope.launch {
            repository.cycleNextDefaultQuote()
            refreshState()
            notifyDefaultWidgetUpdate()
        }
    }

    fun fetchDefaultQuotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetching = true, userMessage = null) }
            val result = repository.fetchAndStoreDefaultQuotes()
            result.onSuccess { added ->
                val msg = if (added > 0) "Fetched $added new quotes from API!" else "Quotes already up to date."
                _uiState.update { it.copy(isFetching = false, userMessage = msg) }
                cycleNextDefaultQuote()
            }.onFailure {
                _uiState.update { it.copy(isFetching = false, userMessage = "Offline mode: Using cached quotes.") }
                cycleNextDefaultQuote()
            }
        }
    }

    fun updateDefaultTheme(themeStyle: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(defaultThemeStyle = themeStyle))
            refreshState()
            notifyDefaultWidgetUpdate()
        }
    }

    fun updateDefaultFontSize(size: Int) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(defaultFontSize = size))
            refreshState()
            notifyDefaultWidgetUpdate()
        }
    }

    fun updateDefaultAlignment(alignment: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(defaultTextAlignment = alignment))
            refreshState()
            notifyDefaultWidgetUpdate()
        }
    }

    fun toggleCategory(category: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            val currentSelected = currentPrefs.selectedCategories.toMutableSet()

            if (category == "All") {
                currentSelected.clear()
                currentSelected.add("All")
            } else {
                currentSelected.remove("All")
                if (currentSelected.contains(category)) {
                    currentSelected.remove(category)
                } else {
                    currentSelected.add(category)
                }
                if (currentSelected.isEmpty()) {
                    currentSelected.add("All")
                }
            }

            repository.savePreferences(currentPrefs.copy(selectedCategories = currentSelected))
            refreshState()
            cycleNextDefaultQuote()
        }
    }

    // Islamic Widget Actions
    fun cycleNextIslamicQuote() {
        viewModelScope.launch {
            repository.cycleNextIslamicQuote()
            refreshState()
            notifyIslamicWidgetUpdate()
        }
    }

    fun fetchIslamicQuotes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetching = true, userMessage = null) }
            val result = repository.fetchAndStoreIslamicQuotes()
            result.onSuccess { added ->
                val msg = if (added > 0) "Fetched $added new Quranic Ayat from API!" else "Islamic library up to date."
                _uiState.update { it.copy(isFetching = false, userMessage = msg) }
                cycleNextIslamicQuote()
            }.onFailure {
                _uiState.update { it.copy(isFetching = false, userMessage = "Offline mode: Using cached Islamic store.") }
                cycleNextIslamicQuote()
            }
        }
    }

    fun updateIslamicTheme(themeStyle: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(islamicThemeStyle = themeStyle))
            refreshState()
            notifyIslamicWidgetUpdate()
        }
    }

    fun updateIslamicFontSize(size: Int) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(islamicFontSize = size))
            refreshState()
            notifyIslamicWidgetUpdate()
        }
    }

    fun updateIslamicAlignment(alignment: String) {
        viewModelScope.launch {
            val currentPrefs = repository.getPreferences()
            repository.savePreferences(currentPrefs.copy(islamicTextAlignment = alignment))
            refreshState()
            notifyIslamicWidgetUpdate()
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun notifyDefaultWidgetUpdate() {
        val context = getApplication<Application>()
        val intent = Intent(context, DefaultWidgetProvider::class.java).apply {
            action = WidgetScheduler.ACTION_REFRESH_DEFAULT_WIDGET
        }
        context.sendBroadcast(intent)
    }

    private fun notifyIslamicWidgetUpdate() {
        val context = getApplication<Application>()
        val intent = Intent(context, IslamicWidgetProvider::class.java).apply {
            action = WidgetScheduler.ACTION_REFRESH_ISLAMIC_WIDGET
        }
        context.sendBroadcast(intent)
    }

    private fun checkOnlineStatus(): Boolean {
        return try {
            val cm = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            val network = cm?.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(network) ?: return false
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } catch (e: Exception) {
            false
        }
    }
}
