package com.grl.sms_wa.works

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.notification.NotificationUtils

class NotificationReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        Log.d("TAG_NOTI_10", "doWork: worker triggered")
        
        val context = applicationContext
        val spManager = SpManager.get(context)
        
        if (!spManager.isCompletedOnboarding) {
            Log.d("TAG_NOTI_10", "doWork: showing onboarding reminder notification")
            NotificationUtils.showRandomOnboardingReminder(context)
        } else {
            Log.d("TAG_NOTI_10", "doWork: already reached main, skipping notification")
        }

        return Result.success()
    }
}
