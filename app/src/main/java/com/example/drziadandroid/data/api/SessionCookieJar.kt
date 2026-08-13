package com.example.drziadandroid.data.api

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import java.util.concurrent.ConcurrentHashMap

class SessionCookieJar(context: Context) : CookieJar {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("cookie_prefs", Context.MODE_PRIVATE)

    // In-memory cache for all cookies (persistent and session)
    private val cookieStore = ConcurrentHashMap<String, Cookie>()

    init {
        loadFromPrefs()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val editor = sharedPreferences.edit()
        for (cookie in cookies) {
            val key = getCookieKey(cookie)
            cookieStore[key] = cookie

            // Only persist cookies that are marked as persistent (have an expiration/max-age)
            if (cookie.persistent) {
                editor.putString(key, serializeCookie(cookie))
            } else {
                // If a non-persistent cookie was previously saved, remove it
                editor.remove(key)
            }
        }
        editor.apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val now = System.currentTimeMillis()
        val validCookies = mutableListOf<Cookie>()
        val expiredKeys = mutableListOf<String>()

        val iterator = cookieStore.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val key = entry.key
            val cookie = entry.value

            if (cookie.expiresAt <= now) {
                expiredKeys.add(key)
                iterator.remove()
            } else if (cookie.matches(url)) {
                validCookies.add(cookie)
            }
        }

        if (expiredKeys.isNotEmpty()) {
            val editor = sharedPreferences.edit()
            for (key in expiredKeys) {
                editor.remove(key)
            }
            editor.apply()
        }

        return validCookies
    }

    fun clear() {
        cookieStore.clear()
        sharedPreferences.edit().clear().apply()
    }

    private fun loadFromPrefs() {
        val now = System.currentTimeMillis()
        val editor = sharedPreferences.edit()
        var hasChanges = false

        val allEntries = sharedPreferences.all
        for ((key, value) in allEntries) {
            if (value is String) {
                val cookie = deserializeCookie(value)
                if (cookie != null) {
                    if (cookie.expiresAt <= now) {
                        editor.remove(key)
                        hasChanges = true
                    } else {
                        cookieStore[key] = cookie
                    }
                } else {
                    editor.remove(key)
                    hasChanges = true
                }
            }
        }
        if (hasChanges) {
            editor.apply()
        }
    }

    private fun getCookieKey(cookie: Cookie): String {
        return "${cookie.name}|${cookie.domain}|${cookie.path}|${cookie.secure}"
    }

    private fun serializeCookie(cookie: Cookie): String {
        return StringBuilder().apply {
            append(cookie.name).append("|")
            append(cookie.value).append("|")
            append(cookie.expiresAt).append("|")
            append(cookie.domain).append("|")
            append(cookie.path).append("|")
            append(if (cookie.secure) "1" else "0").append("|")
            append(if (cookie.httpOnly) "1" else "0").append("|")
            append(if (cookie.hostOnly) "1" else "0").append("|")
            append(if (cookie.persistent) "1" else "0")
        }.toString()
    }

    private fun deserializeCookie(serialized: String): Cookie? {
        val parts = serialized.split("|")
        if (parts.size < 9) return null
        return try {
            val builder = Cookie.Builder()
                .name(parts[0])
                .value(parts[1])
                .expiresAt(parts[2].toLong())

            val domain = parts[3]
            if (parts[7] == "1") {
                builder.hostOnlyDomain(domain)
            } else {
                builder.domain(domain)
            }

            builder.path(parts[4])
            if (parts[5] == "1") builder.secure()
            if (parts[6] == "1") builder.httpOnly()

            builder.build()
        } catch (e: Exception) {
            null
        }
    }
}
