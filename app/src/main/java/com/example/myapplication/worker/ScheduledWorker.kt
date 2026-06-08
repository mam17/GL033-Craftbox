package com.example.myapplication.worker

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.utils.SpManager

class ScheduledWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val id = inputData.getString("id") ?: return Result.failure()
        val address = inputData.getString("address") ?: return Result.failure()
        val body = inputData.getString("body") ?: return Result.failure()
        val subId = inputData.getInt("subId", -1)

        return try {
            val smsManager = getSmsManager(applicationContext, subId)
            smsManager.sendTextMessage(address, null, body, null, null)

            val spManager = SpManager.get(applicationContext)
            spManager.markScheduledMessageSent(id)

            val smsRepository = SmsRepository()
            smsRepository.insertSentSms(
                applicationContext,
                address,
                body,
                System.currentTimeMillis()
            )
            smsRepository.notifyDataChanged()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun getSmsManager(context: Context, subscriptionId: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val sm = context.getSystemService(SmsManager::class.java)
            if (subscriptionId != -1) sm.createForSubscriptionId(subscriptionId) else sm
        } else {
            @Suppress("DEPRECATION")
            if (subscriptionId != -1) SmsManager.getSmsManagerForSubscriptionId(subscriptionId) else SmsManager.getDefault()
        }
    }
}
