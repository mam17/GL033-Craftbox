package com.example.myapplication.utils.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.example.myapplication.R
import com.example.myapplication.ui.components.mess.activity.MessengerActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ContactUtils
import com.example.myapplication.utils.SpManager

object NotificationSMS {

    private const val CHANNEL_ID = "sms_channel_id"
    private const val CHANNEL_NAME = "SMS Notifications"

    fun showNotification(context: Context, address: String, body: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MessengerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(Constant.EXTRA_ADDRESS, address)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            address.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contactInfo = ContactUtils.getContactInfo(context, address)
        val spManager = SpManager.get(context)
        val previewMode = spManager.getNotificationPreviewMode(address)

        val displayTitle: String
        val displayBody: String

        when (previewMode) {
            1 -> { // Show name only
                displayTitle = contactInfo.name
                displayBody = context.getString(R.string.txt_new_message)
            }
            2 -> { // Hide contents
                displayTitle = context.getString(R.string.txt_private)
                displayBody = context.getString(R.string.txt_new_message)
            }
            else -> { // Show name and message
                displayTitle = contactInfo.name
                displayBody = body
            }
        }

        val remoteViewsCollapsed = RemoteViews(context.packageName, R.layout.notification_collapsed)
        val remoteViewsExpanded = RemoteViews(context.packageName, R.layout.notification_expanded)

        // Setup collapsed view
        remoteViewsCollapsed.setTextViewText(R.id.tvName, displayTitle)
        remoteViewsCollapsed.setTextViewText(R.id.tvBody, displayBody)

        // Setup expanded view
        remoteViewsExpanded.setTextViewText(R.id.tvName, displayTitle)
        remoteViewsExpanded.setTextViewText(R.id.tvBody, displayBody)

        if (contactInfo.photo != null) {
            remoteViewsCollapsed.setImageViewBitmap(R.id.ivAvatar, contactInfo.photo)
            remoteViewsExpanded.setImageViewBitmap(R.id.ivAvatar, contactInfo.photo)
        } else {
            remoteViewsCollapsed.setImageViewResource(R.id.ivAvatar, R.drawable.ic_person)
            remoteViewsExpanded.setImageViewResource(R.id.ivAvatar, R.drawable.ic_person)
        }

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCustomContentView(remoteViewsCollapsed)
            .setCustomBigContentView(remoteViewsExpanded)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())

        val notification = notificationBuilder.build()
        notificationManager.notify(address.hashCode(), notification)
    }

    fun cancelNotification(context: Context, address: String) {
        if (address.isBlank()) return
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(address.hashCode())
    }
}
