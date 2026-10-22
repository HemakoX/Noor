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
                
                // Cycle next quote for Default and Islamic pools
                repository.cycleNextDefaultQuote()
                repository.cycleNextIslamicQuote()

                val appWidgetManager = AppWidgetManager.getInstance(context)

                // Refresh all Default Widgets
                val defaultComponent = ComponentName(context, DefaultWidgetProvider::class.java)
                val defaultIds = appWidgetManager.getAppWidgetIds(defaultComponent)
                for (id in defaultIds) {
                    DefaultWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                }

                // Refresh all Islamic Widgets
                val islamicComponent = ComponentName(context, IslamicWidgetProvider::class.java)
                val islamicIds = appWidgetManager.getAppWidgetIds(islamicComponent)
                for (id in islamicIds) {
                    IslamicWidgetProvider.updateAppWidget(context, appWidgetManager, id)
                }
            } catch (_: Exception) {
                // Safe error handling to prevent any crash on unlock
            }
        }
    }
}
