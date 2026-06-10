package com.grl.sms_wa.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.grl.sms_wa.ui.alertfull.NotificationFSUtil
import com.grl.sms_wa.utils.ScheduledMessageScheduler
import com.grl.sms_wa.utils.SpManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            NotificationFSUtil.scheduleFullScreenNotificationDiary(context)
            rescheduleMessages(context)
        }
    }

    private fun rescheduleMessages(context: Context) {
        val now = System.currentTimeMillis()
        SpManager.get(context).getScheduledMessages()
            .filter { !it.isSent }
            .forEach { message ->
                ScheduledMessageScheduler.schedule(
                    context = context,
                    id = message.id,
                    address = message.address,
                    body = message.body,
                    scheduledTime = message.scheduledTime.coerceAtLeast(now + 1_000L),
                    subscriptionId = message.subId
                )
            }
    }
}
