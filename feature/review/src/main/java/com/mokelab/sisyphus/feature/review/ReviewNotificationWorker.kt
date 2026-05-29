package com.mokelab.sisyphus.feature.review

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mokelab.sisyphus.core.database.repository.ReviewCardRepository

class ReviewNotificationWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: ReviewCardRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "review_reminders"
        const val NOTIFICATION_ID = 1001
    }

    override suspend fun doWork(): Result {
        val dueCount = repository.getDueCount()

        if (dueCount > 0) {
            showNotification(dueCount)
        }

        return Result.success()
    }

    private fun showNotification(dueCount: Int) {
        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "复习提醒",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "提醒您复习到期的卡片"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("复习提醒")
            .setContentText("您有 $dueCount 张卡片需要复习")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
