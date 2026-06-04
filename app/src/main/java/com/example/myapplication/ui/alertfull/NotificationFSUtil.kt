package com.example.myapplication.ui.alertfull

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.example.myapplication.R
import com.example.myapplication.receiver.FullScreenReceiver
import com.example.myapplication.ui.splash.SplashActivity
import java.util.Calendar

object NotificationFSUtil {

    private const val CHANNEL_ID = "FULL_SCREEN_CHANNEL"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Full Screen Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for full screen notifications"
                enableVibration(true)
                enableLights(true)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }

            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }


    fun createFullScreenIntent(context: Context): PendingIntent {
        val intent = Intent(context, AlertFullScreenActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun createContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, SplashActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    @SuppressLint("FullScreenIntentPolicy")
    fun showFullScreenNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val canFSI = if (Build.VERSION.SDK_INT >= 34) nm?.canUseFullScreenIntent() == true else true

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.txt_title_noti_full))
            .setContentText(context.getString(R.string.txt_body_noti_full))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSound(Settings.System.DEFAULT_RINGTONE_URI)
            .setContentIntent(createContentIntent(context))

        if (canFSI) {
            builder.setFullScreenIntent(createFullScreenIntent(context), true)
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val settingsPending = PendingIntent.getActivity(
                context, 123, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(
                0,
                context.getString(R.string.txt_enable_full_screen_permission),
                settingsPending
            )
        }

        nm?.notify(1, builder.build())
    }


//    fun scheduleFullScreenNotificationAfterExit(context: Context) {
//        val work = OneTimeWorkRequestBuilder<NotificationWorker>()
//            .setInitialDelay(10, TimeUnit.MINUTES)
//            .addTag("full_screen_notification")
//            .build()
//        Log.i("TAG_NOTI_AAA", "scheduleFullScreenNotificationAfterExit: ")
//        WorkManager.getInstance(context).enqueue(work)
//    }

    @SuppressLint("ScheduleExactAlarm")
    fun scheduleFullScreenNotificationDiary(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (!ensureExactAlarmPermission(context)) {
//            Toast.makeText(context, context.getString(R.string.txt_no_alarm_permission_yet), Toast.LENGTH_LONG).show()
            Log.i(
                "TAG_ALARM_PERMISSION",
                "scheduleFullScreenNotificationDiary: No alarm permission yet"
            )
            return
        }
        val times = listOf(
            getNextTriggerTime(11, 52),
            getNextTriggerTime(21, 0)
        )

        for (triggerTime in times) {
            val intent = Intent(context, FullScreenReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                triggerTime.timeInMillis.toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime.timeInMillis,
                pendingIntent
            )
        }
    }

    /**
     * Tính thời điểm kích hoạt tiếp theo cho giờ và phút cho trước.
     */
    private fun getNextTriggerTime(hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }

    private fun ensureExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            val hasPermission = alarmManager.canScheduleExactAlarms()
//            if (!hasPermission) {
//                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
//                    data = "package:${context.packageName}".toUri()
//                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                }
//                context.startActivity(intent)
//            }
            return hasPermission
        }
        return true
    }

}
