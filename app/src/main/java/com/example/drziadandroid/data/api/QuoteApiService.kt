package com.example.drziadandroid.data.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

@Serializable
data class DummyJsonResponse(
    val quotes: List<DummyJsonQuoteItem> = emptyList(),
    val total: Int = 0,
    val skip: Int = 0,
    val limit: Int = 0
)

@Serializable
data class DummyJsonQuoteItem(
    val id: Int = 0,
    val quote: String = "",
    val author: String = ""
)

@Serializable
data class ZenQuoteItem(
    val q: String = "",
    val a: String = ""
)

@Serializable
data class AlQuranAyahResponse(
    val code: Int = 0,
    val status: String = "",
    val data: AlQuranAyahData? = null
)

@Serializable
data class AlQuranAyahData(
    val number: Int = 0,
    val text: String = "",
    val numberInSurah: Int = 0,
    val surah: AlQuranSurah? = null
)

@Serializable
data class AlQuranSurah(
    val number: Int = 0,
    val name: String = "",
    val englishName: String = ""
)

interface QuoteApiService {
    @GET("quotes")
    suspend fun getDummyJsonQuotes(
        @Query("limit") limit: Int = 10,
        @Query("skip") skip: Int = 0
    ): DummyJsonResponse

    @GET
    suspend fun getZenQuotes(@Url url: String = "https://zenquotes.io/api/quotes"): List<ZenQuoteItem>

    @GET
    suspend fun getQuranAyah(@Url url: String): AlQuranAyahResponse
}
