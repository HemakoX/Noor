package com.example.drziadandroid.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.drziadandroid.data.repository.QuoteRepository

object WidgetScheduler {

    const val ACTION_REFRESH_DEFAULT_WIDGET = "com.example.drziadandroid.ACTION_REFRESH_DEFAULT_WIDGET"
    const val ACTION_REFRESH_ISLAMIC_WIDGET = "com.example.drziadandroid.ACTION_REFRESH_ISLAMIC_WIDGET"

    fun schedulePeriodicUpdates(context: Context) {
        try {
            val repository = QuoteRepository(context)
            val prefs = repository.getPreferences()
            val interval = prefs.refreshIntervalMillis

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Default Widget Alarm
            val defaultIntent = Intent(context, DefaultWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_DEFAULT_WIDGET
            }
            val defaultPendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                defaultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(defaultPendingIntent)

            if (interval > 0L) {
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + interval,
                    interval,
                    defaultPendingIntent
                )
            }

            // Islamic Widget Alarm
            val islamicIntent = Intent(context, IslamicWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_ISLAMIC_WIDGET
            }
            val islamicPendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                islamicIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.cancel(islamicPendingIntent)

            if (interval > 0L) {
                alarmManager.setInexactRepeating(
                    AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + interval,
                    interval,
                    islamicPendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
