package com.example.drziadandroid.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.TypedValue
import android.view.Gravity
import android.widget.RemoteViews
import com.example.drziadandroid.MainActivity
import com.example.drziadandroid.R
import com.example.drziadandroid.data.model.Quote
import com.example.drziadandroid.data.model.WidgetPreferences
import com.example.drziadandroid.data.repository.QuoteRepository

class IslamicWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        WidgetScheduler.schedulePeriodicUpdates(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val action = intent.action
        if (action == WidgetScheduler.ACTION_REFRESH_ISLAMIC_WIDGET ||
            action == AppWidgetManager.ACTION_APPWIDGET_UPDATE ||
            action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED
        ) {
            if (action == WidgetScheduler.ACTION_REFRESH_ISLAMIC_WIDGET) {
                try {
                    val repository = QuoteRepository(context)
                    repository.cycleNextIslamicQuote()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, IslamicWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
            WidgetScheduler.schedulePeriodicUpdates(context)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetScheduler.schedulePeriodicUpdates(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            try {
                val repository = QuoteRepository(context)
                val quote = try {
                    repository.getCurrentIslamicQuote()
                } catch (_: Exception) {
                    Quote("fallback_q", "", "سورة البقرة [2:152]", "Quran", "فَاذْكُرُونِي أَذْكُرْكُمْ")
                }

                val prefs = try {
                    repository.getPreferences()
                } catch (_: Exception) {
                    WidgetPreferences()
                }

                val views = RemoteViews(context.packageName, R.layout.widget_quotely)

                // Arabic-Only display text
                val displayQuoteText = if (!quote.arabicText.isNullOrBlank()) {
                    quote.arabicText
                } else {
                    quote.text
                }

                // Compact Arabic reference (strip English portion)
                val displayAuthor = if (quote.author.contains(" •")) {
                    quote.author.substringBefore(" •")
                } else {
                    quote.author
                }

                val categoryLabel = when (quote.category) {
                    "Quran" -> "آية قرآنية"
                    "Hadith" -> "حديث شريف"
                    else -> "إسلامي"
                }

                views.setTextViewText(R.id.widget_quote_text, displayQuoteText)
                views.setTextViewText(R.id.widget_author_text, displayAuthor)
                views.setTextViewText(R.id.widget_category_text, "NOOR • $categoryLabel")

                // Background Theme
                val bgRes = when (prefs.islamicThemeStyle) {
                    "Light" -> R.drawable.widget_bg_light
                    "Sunset" -> R.drawable.widget_bg_sunset
                    "Midnight" -> R.drawable.widget_bg_midnight
                    "Dark" -> R.drawable.widget_bg_dark
                    "Glass" -> R.drawable.widget_bg_glass
                    else -> R.drawable.widget_bg_emerald // Default for Islamic
                }
                views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)

                // Text Colors
                val isLight = prefs.islamicThemeStyle == "Light"
                val textColor = if (isLight) 0xFF1E293B.toInt() else 0xFFFFFFFF.toInt()
                val authorColor = if (isLight) 0xFF475569.toInt() else 0xFFCBD5E0.toInt()
                val badgeColor = if (isLight) 0xFF64748B.toInt() else 0xFFA0AEC0.toInt()

                views.setTextColor(R.id.widget_quote_text, textColor)
                views.setTextColor(R.id.widget_author_text, authorColor)
                views.setTextColor(R.id.widget_category_text, badgeColor)

                // Text Size
                views.setTextViewTextSize(
                    R.id.widget_quote_text,
                    TypedValue.COMPLEX_UNIT_SP,
                    prefs.islamicFontSize.toFloat()
                )

                // Alignment
                val gravity = when (prefs.islamicTextAlignment) {
                    "Left" -> Gravity.START or Gravity.CENTER_VERTICAL
                    "Right" -> Gravity.END or Gravity.CENTER_VERTICAL
                    else -> Gravity.CENTER
                }
                views.setInt(R.id.widget_quote_text, "setGravity", gravity)
                views.setInt(R.id.widget_author_text, "setGravity", gravity)

                // Pending Intent: Refresh
                val refreshIntent = Intent(context, IslamicWidgetProvider::class.java).apply {
                    action = WidgetScheduler.ACTION_REFRESH_ISLAMIC_WIDGET
                }
                val refreshPendingIntent = PendingIntent.getBroadcast(
                    context,
                    appWidgetId,
                    refreshIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_btn_refresh, refreshPendingIntent)

                // Pending Intent: Open App
                val appIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                val appPendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    appIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_root, appPendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
