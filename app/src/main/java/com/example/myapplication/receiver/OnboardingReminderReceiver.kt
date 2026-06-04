package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.utils.notification.NotificationUtils
import com.example.myapplication.utils.SpManager

class OnboardingReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val spManager = SpManager.get(context)

        when (intent?.action) {
            NotificationUtils.ACTION_ONBOARDING_REMINDER_DISMISSED -> {
                if (!spManager.isCompletedOnboarding) {
                    NotificationUtils.scheduleOnboardingReminderAfterDismiss(context)
                }
            }

            else -> {
                if (!spManager.isCompletedOnboarding) {
                    NotificationUtils.scheduleOnboardingReminder(context)
                }
            }
        }
    }
}
