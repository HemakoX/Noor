package com.example.drziadandroid.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock

object WidgetScheduler {

    const val ACTION_REFRESH_DEFAULT_WIDGET = "com.example.drziadandroid.ACTION_REFRESH_DEFAULT_WIDGET"
    const val ACTION_REFRESH_ISLAMIC_WIDGET = "com.example.drziadandroid.ACTION_REFRESH_ISLAMIC_WIDGET"

    fun schedulePeriodicUpdates(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // Schedule Default Widget Alarms
            val defaultIntent = Intent(context, DefaultWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_DEFAULT_WIDGET
            }
            val defaultPendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                defaultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                defaultPendingIntent
            )

            // Schedule Islamic Widget Alarms
            val islamicIntent = Intent(context, IslamicWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_ISLAMIC_WIDGET
            }
            val islamicPendingIntent = PendingIntent.getBroadcast(
                context,
                1002,
                islamicIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR,
                AlarmManager.INTERVAL_HOUR,
                islamicPendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
