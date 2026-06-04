package com.example.myapplication.utils.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.myapplication.R
import com.example.myapplication.receiver.OnboardingReminderReceiver
import com.example.myapplication.ui.splash.SplashActivity
import com.example.myapplication.utils.FirebaseTrackingManager
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.SpManager
import com.example.myapplication.domain.layer.NotificationContentModel
import com.example.myapplication.works.NotificationReminderWorker

object NotificationUtils {
    private const val TAG = "TAG_NOTI_10"
    private const val CHANNEL_ID = "rbx_reminder_channel"
    private const val CHANNEL_NAME = "RBX reminders"
    private const val ONBOARDING_REMINDER_DISMISS_REQUEST_CODE = 1002
    private const val NOTIFICATION_ID = 1003
    private const val ONBOARDING_REMINDER_DELAY_MS = 3_000L
    private const val ONBOARDING_REMINDER_DISMISSED_DELAY_MS = 20_000L
    private const val WORK_NAME = "onboarding_reminder_notification_work"
    const val KEY_NOTIFICATION_DELAY_MS = "notification_delay_ms"
    const val ACTION_ONBOARDING_REMINDER_DISMISSED =
        "com.example.myapplication.action.ONBOARDING_REMINDER_DISMISSED"
    const val EXTRA_OPEN_FROM_NOTIFICATION = "open_from_notification"

    fun scheduleOnboardingReminder(context: Context) {
        scheduleWithWorkManager(context, ONBOARDING_REMINDER_DELAY_MS)
    }

    fun scheduleOnboardingReminderAfterDismiss(context: Context) {
        scheduleWithWorkManager(context, ONBOARDING_REMINDER_DISMISSED_DELAY_MS)
    }

    fun cancelOnboardingReminder(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun scheduleWithWorkManager(context: Context, delayMs: Long) {
        if (!PermissionUtils.isNotificationPermissionGranted(context)) {
            Log.d(TAG, "scheduleWithWorkManager: permission not granted")
            return
        }
        val spManager = SpManager.get(context)
        if (spManager.isCompletedOnboarding || isNotificationActive(context)) {
            Log.d(TAG, "scheduleWithWorkManager: reached main or notification active")
            return
        }

        val workRequest = OneTimeWorkRequestBuilder<NotificationReminderWorker>()
            .setInitialDelay(delayMs, java.util.concurrent.TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        Log.i(TAG, "scheduleWithWorkManager: enqueued worker with delay $delayMs ms")
    }

    suspend fun showRandomOnboardingReminder(context: Context) {
        if (!PermissionUtils.isNotificationPermissionGranted(context)) return
        val spManager = SpManager.get(context)
        if (spManager.isCompletedOnboarding || isNotificationActive(context)) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)
        val content = NotificationContentProvider.getRandomContent(context)
        val intent = Intent(context, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_OPEN_FROM_NOTIFICATION, true)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val deleteIntent = PendingIntent.getBroadcast(
            context,
            ONBOARDING_REMINDER_DISMISS_REQUEST_CODE,
            Intent(context, OnboardingReminderReceiver::class.java).apply {
                action = ACTION_ONBOARDING_REMINDER_DISMISSED
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val titleText = context.getString(content.resTitle)
        val bodyText = context.getString(content.resBody)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titleText)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setContentIntent(pendingIntent)
            .setDeleteIntent(deleteIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }.onSuccess {
            FirebaseTrackingManager.instance()
                .logEvent(FirebaseTrackingManager.EVENT_NOTIFICATION_PUSH_SUCCESS)
            Log.i(TAG, "showRandomOnboardingReminder: push success")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Kiểm tra notification có đang hiện trên màn hình không.
     */
    private fun isNotificationActive(context: Context): Boolean {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        return notificationManager.activeNotifications.any { it.id == NOTIFICATION_ID }
    }
}
