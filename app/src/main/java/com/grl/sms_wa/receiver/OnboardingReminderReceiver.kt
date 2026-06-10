package com.grl.sms_wa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.grl.sms_wa.utils.notification.NotificationUtils
import com.grl.sms_wa.utils.SpManager

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
