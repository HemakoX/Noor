package com.example.drziadandroid.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.drziadandroid.data.repository.QuoteRepository

class DeviceUnlockReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_USER_PRESENT) {
            try {
                val repository = QuoteRepository(context)
                val prefs = repository.getPreferences()

                // Only refresh on unlock if the user enabled the setting in app preferences
                if (!prefs.refreshOnUnlock) return

                repository.cycleNextDefaultQuote()
                repository.cycleNextIslamicQuote()

                val appWidgetManager = AppWidgetManager.getInstance(context)

                val defaultComponent = ComponentName(context, DefaultWidgetProvider::class.java)
                val defaultIds = appWidgetManager.getAppWidgetIds(defaultComponent)
                for (id in defaultIds) {
                    DefaultWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                }

                val islamicComponent = ComponentName(context, IslamicWidgetProvider::class.java)
                val islamicIds = appWidgetManager.getAppWidgetIds(islamicComponent)
                for (id in islamicIds) {
                    IslamicWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {
                // Safe handling
            }
        }
    }
}
