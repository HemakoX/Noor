package com.example.drziadandroid

import android.app.Application
import com.example.drziadandroid.widget.WidgetScheduler

class QuotelyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        WidgetScheduler.schedulePeriodicUpdates(this)
    }
}
