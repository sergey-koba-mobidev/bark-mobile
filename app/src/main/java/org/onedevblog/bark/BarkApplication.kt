package org.onedevblog.bark

import android.app.Application
import android.content.Context

class BarkApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        context = getApplicationContext()
    }

    companion object {
        lateinit var context: Context
    }
}