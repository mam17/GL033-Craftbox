package com.grl.sms_wa.service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class HeadlessSmsSendService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Required for default SMS app implementation
        // Handles responding via message when a call is incoming
        
        // After processing, stop the service as it's no longer needed
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
