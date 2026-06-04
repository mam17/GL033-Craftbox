package com.example.myapplication.ui.alertfull

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class NotificationFSWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        Log.d("TAG_NotificationWorker", "Bắt đầu hiển thị notification sau 30 giây")

        try {
            NotificationFSUtil.showFullScreenNotification(applicationContext)
            Log.d("TAG_NotificationWorker", "Đã hiển thị notification thành công")
            return Result.success()
        } catch (e: Exception) {
            Log.e("TAG_NotificationWorker", "Lỗi khi hiển thị notification: ${e.message}")
            return Result.failure()
        }
    }
}