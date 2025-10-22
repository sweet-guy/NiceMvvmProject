package com.example.nicemvvmproject

import android.app.Application
import com.example.nicemvvmproject.network.RequestParamsProvider
import com.example.nicemvvmproject.network.RetrofitClient
import com.example.nicemvvmproject.room.RoomHelper
import timber.log.Timber

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.LOG_ENABLE) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
        RoomHelper.init(this)
        // 初始化全局公共参数/头拦截器（可按需修改）
        RetrofitClient.setRequestParamsProvider(object : RequestParamsProvider {
            override fun provideHeaders(): Map<String, String> {
                return mapOf(
                    // "Authorization" to "Bearer your_token",
                    "X-App-Platform" to "android"
                )
            }

            override fun provideQueryParams(): Map<String, String> {
                return mapOf(
                    "appVersion" to BuildConfig.VERSION_NAME
                )
            }
        })
    }
}

class ReleaseTree : Timber.Tree() {
    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log WARN and above in release
        return priority >= android.util.Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        if (!isLoggable(tag, priority)) return
        android.util.Log.println(priority, tag, message)
        t?.let { android.util.Log.println(priority, tag, android.util.Log.getStackTraceString(it)) }
    }
}


