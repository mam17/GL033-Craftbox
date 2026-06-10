package com.grl.sms_wa.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.receiver.ScheduledMessageReceiver

object ScheduledMessageScheduler {
    private const val ACTION_SEND_SCHEDULED_MESSAGE =
        "com.example.myapplication.action.SEND_SCHEDULED_MESSAGE"

    const val EXTRA_ID = "extra_scheduled_id"
    const val EXTRA_ADDRESS = "extra_scheduled_address"
    const val EXTRA_BODY = "extra_scheduled_body"
    const val EXTRA_SUBSCRIPTION_ID = "extra_scheduled_subscription_id"

    fun schedule(
        context: Context,
        id: String,
        address: String,
        body: String,
        scheduledTime: Long,
        subscriptionId: Int
    ) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = createPendingIntent(
            context = context,
            id = id,
            address = address,
            body = body,
            subscriptionId = subscriptionId,
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        ) ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                alarmManager?.canScheduleExactAlarms() != true
            ) {
                alarmManager?.setAlarmClock(
                    AlarmManager.AlarmClockInfo(scheduledTime, createShowPendingIntent(context)),
                    pendingIntent
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    scheduledTime,
                    pendingIntent
                )
            } else {
                alarmManager?.setExact(AlarmManager.RTC_WAKEUP, scheduledTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            alarmManager?.setAlarmClock(
                AlarmManager.AlarmClockInfo(scheduledTime, createShowPendingIntent(context)),
                pendingIntent
            )
        }
    }

    fun cancel(context: Context, id: String) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val pendingIntent = createPendingIntent(
            context = context,
            id = id,
            address = "",
            body = "",
            subscriptionId = -1,
            flags = PendingIntent.FLAG_NO_CREATE
        )
        pendingIntent?.let {
            alarmManager?.cancel(it)
            it.cancel()
        }
    }

    private fun createPendingIntent(
        context: Context,
        id: String,
        address: String,
        body: String,
        subscriptionId: Int,
        flags: Int
    ): PendingIntent? {
        val intent = Intent(context, ScheduledMessageReceiver::class.java).apply {
            action = ACTION_SEND_SCHEDULED_MESSAGE
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_ADDRESS, address)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
        }
        return PendingIntent.getBroadcast(
            context,
            id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or flags
        )
    }

    private fun createShowPendingIntent(context: Context): PendingIntent {
        return PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
