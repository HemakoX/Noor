package com.example.drziadandroid

import android.app.Application
import com.example.drziadandroid.data.api.RetrofitClient

class DrZiadApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
