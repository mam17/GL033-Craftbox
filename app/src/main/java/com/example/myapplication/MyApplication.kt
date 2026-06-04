package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.DefaultLifecycleObserver
import com.example.myapplication.utils.AppEx.setupAppShortcuts
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.notification.NotificationUtils
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Singleton

@HiltAndroidApp
@Singleton
class MyApplication : Application(), Application.ActivityLifecycleCallbacks,
    DefaultLifecycleObserver {
    private var startedActivityCount = 0

    companion object {
        private const val TAG = "TAG_MyApplication"

        @SuppressLint("StaticFieldLeak")
        var context: Context? = null

        @SuppressLint("StaticFieldLeak")
        private var mInstance: MyApplication? = null
        val instance get() = mInstance
    }

    override fun onCreate() {
        super<Application>.onCreate()
        mInstance = this
        context = applicationContext
        setupAppShortcuts(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivityPaused(p0: Activity) {
    }

    override fun onActivityResumed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStarted(p0: Activity) {
        startedActivityCount++
    }

    override fun onActivityStopped(activity: Activity) {
        if (activity.isChangingConfigurations) return

        startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
        if (startedActivityCount == 0) {
            val sp = SpManager.get(applicationContext)
            if (!sp.isCompletedOnboarding) {
                NotificationUtils.scheduleOnboardingReminder(applicationContext)
            }
        }
    }
}
