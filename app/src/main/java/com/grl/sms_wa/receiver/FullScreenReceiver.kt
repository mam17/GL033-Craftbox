package com.grl.sms_wa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.grl.sms_wa.ui.alertfull.NotificationFSUtil

class FullScreenReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        NotificationFSUtil.showFullScreenNotification(context)
    }
}