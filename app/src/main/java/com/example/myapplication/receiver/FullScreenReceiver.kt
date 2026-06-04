package com.example.myapplication.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.myapplication.ui.alertfull.NotificationFSUtil

class FullScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationFSUtil.showFullScreenNotification(context)
    }
}