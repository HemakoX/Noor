package com.example.drziadandroid.data.repository

import android.content.Context
import com.example.drziadandroid.data.api.RetrofitClient
import com.example.drziadandroid.data.local.LocalQuoteStore
import com.example.drziadandroid.data.model.Quote
import com.example.drziadandroid.data.model.WidgetPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import kotlin.random.Random

class QuoteRepository(context: Context) {

    private val localStore = LocalQuoteStore(context.applicationContext)
    private val categoriesList = listOf("Motivation", "Philosophy", "Science", "Life", "Wisdom", "Success", "Humor", "Art", "Tech")

    fun getAllQuotes(): List<Quote> = localStore.getAllQuotes()

    fun getPreferences(): WidgetPreferences = localStore.getPreferences()

    fun savePreferences(prefs: WidgetPreferences) {
        localStore.savePreferences(prefs)
    }

    // Default Quotes Filter
    fun getFilteredDefaultQuotes(): List<Quote> {
        val all = localStore.getAllQuotes()
        val prefs = getPreferences()

        val defaultPool = all.filter { it.category != "Quran" && it.category != "Hadith" && it.category != "Islamic" }
        val selected = prefs.selectedCategories

        if (selected.contains("All") || selected.isEmpty()) {
            return if (defaultPool.isEmpty()) all else defaultPool
        }

        val filtered = defaultPool.filter { selected.contains(it.category) }
        return if (filtered.isEmpty()) defaultPool else filtered
    }

    // Islamic Quotes Filter
    fun getIslamicQuotes(): List<Quote> {
        val all = localStore.getAllQuotes()
        val islamic = all.filter { it.category == "Quran" || it.category == "Hadith" || it.category == "Islamic" }
        return if (islamic.isEmpty()) all else islamic
    }

    // Default Quote Selection & Cycling
    fun getCurrentDefaultQuote(): Quote {
        val prefs = getPreferences()
        val filtered = getFilteredDefaultQuotes()

        if (filtered.isEmpty()) {
            return Quote("default", "The journey of a thousand miles begins with one step.", "Lao Tzu", "Motivation")
        }

        if (prefs.currentDefaultQuoteId != null) {
            val matched = filtered.find { it.id == prefs.currentDefaultQuoteId }
            if (matched != null) return matched
        }

        val randomQuote = filtered.random()
        savePreferences(prefs.copy(currentDefaultQuoteId = randomQuote.id))
        return randomQuote
    }

    fun cycleNextDefaultQuote(): Quote {
        val prefs = getPreferences()
        val filtered = getFilteredDefaultQuotes()
        if (filtered.isEmpty()) return getCurrentDefaultQuote()

        val currentIndex = filtered.indexOfFirst { it.id == prefs.currentDefaultQuoteId }
        val nextIndex = if (currentIndex == -1 || currentIndex >= filtered.size - 1) 0 else currentIndex + 1

        val nextQuote = filtered[nextIndex]
        savePreferences(prefs.copy(currentDefaultQuoteId = nextQuote.id))
        return nextQuote
    }

    // Islamic Quote Selection & Cycling
    fun getCurrentIslamicQuote(): Quote {
        val prefs = getPreferences()
        val islamic = getIslamicQuotes()

        if (islamic.isEmpty()) {
            return Quote("q1", "No quotes", "سورة البقرة [2:152] • Surah Al-Baqarah [2:152]", "Quran", "فَاذْكُرُونِي أَذْكُرْكُمْ")
        }

        if (prefs.currentIslamicQuoteId != null) {
            val matched = islamic.find { it.id == prefs.currentIslamicQuoteId }
            if (matched != null) return matched
        }

        val randomQuote = islamic.random()
        savePreferences(prefs.copy(currentIslamicQuoteId = randomQuote.id))
        return randomQuote
    }

    fun cycleNextIslamicQuote(): Quote {
        val prefs = getPreferences()
        val islamic = getIslamicQuotes()
        if (islamic.isEmpty()) return getCurrentIslamicQuote()

        val currentIndex = islamic.indexOfFirst { it.id == prefs.currentIslamicQuoteId }
        val nextIndex = if (currentIndex == -1 || currentIndex >= islamic.size - 1) 0 else currentIndex + 1

        val nextQuote = islamic[nextIndex]
        savePreferences(prefs.copy(currentIslamicQuoteId = nextQuote.id))
        return nextQuote
    }

    // Fetching Default Quotes
    suspend fun fetchAndStoreDefaultQuotes(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val skip = Random.nextInt(0, 100)
            val response = RetrofitClient.quoteApiService.getDummyJsonQuotes(limit = 10, skip = skip)
            if (response.quotes.isEmpty()) {
                return@withContext Result.failure(Exception("Empty API response"))
            }

            val newQuotes = response.quotes.mapIndexed { index, item ->
                val category = categoriesList[(index + skip) % categoriesList.size]
                Quote(
                    id = "api_${item.id}_${UUID.randomUUID().toString().take(6)}",
                    text = item.quote.trim(),
                    author = item.author.trim().ifEmpty { "Unknown" },
                    category = category
                )
            }

            val added = localStore.addQuotes(newQuotes)
            Result.success(added)
        } catch (e: Exception) {
            try {
                val zenQuotes = RetrofitClient.quoteApiService.getZenQuotes()
                if (zenQuotes.isNotEmpty()) {
                    val newQuotes = zenQuotes.take(10).mapIndexed { index, item ->
                        val category = categoriesList[index % categoriesList.size]
                        Quote(
                            id = "zen_${UUID.randomUUID().toString().take(8)}",
                            text = item.q.trim(),
                            author = item.a.trim().ifEmpty { "Unknown" },
                            category = category
                        )
                    }
                    val added = localStore.addQuotes(newQuotes)
                    Result.success(added)
                } else {
                    Result.failure(e)
                }
            } catch (_: Exception) {
                Result.failure(e)
            }
        }
    }

    // Fetching Islamic Quotes
    suspend fun fetchAndStoreIslamicQuotes(): Result<Int> = withContext(Dispatchers.IO) {
        val newQuranQuotes = mutableListOf<Quote>()
        val sampleAyahNumbers = listOf(255, 152, 286, 186, 153, 201, 139, 28, 3, 7, 30, 87, 5, 25, 156).shuffled().take(10)

        for (ayahNum in sampleAyahNumbers) {
            try {
                val response = RetrofitClient.quoteApiService.getQuranAyah("https://api.alquran.cloud/v1/ayah/$ayahNum/quran-uthmani")
                if (response.code == 200 && response.data != null) {
                    val data = response.data
                    val surahName = data.surah?.name ?: ""
                    val surahEng = data.surah?.englishName ?: ""
                    val verseNum = data.numberInSurah

                    newQuranQuotes.add(
                        Quote(
                            id = "quran_online_$ayahNum",
                            text = "Surah $surahEng [$verseNum]",
                            author = "سورة $surahName [$verseNum] • Surah $surahEng [$verseNum]",
                            category = "Quran",
                            arabicText = data.text
                        )
                    )
                }
            } catch (_: Exception) {
                // Ignore individual network failures
            }
        }

        if (newQuranQuotes.isNotEmpty()) {
            val added = localStore.addQuotes(newQuranQuotes)
            Result.success(added)
        } else {
            val islamicQuotes = getIslamicQuotes()
            Result.success(islamicQuotes.size)
        }
    }
}
