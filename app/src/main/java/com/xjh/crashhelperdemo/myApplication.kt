package com.xjh.crashhelperdemo

import android.app.Application

class myApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        CrashHandler.instance.init(this)
    }
}