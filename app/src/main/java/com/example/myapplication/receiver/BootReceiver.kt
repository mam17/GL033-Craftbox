package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.ui.alertfull.NotificationFSUtil

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationFSUtil.scheduleFullScreenNotificationDiary(context)
        }
    }
}
