package com.example.winly

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.winly.NotificationHelper

class DeadlineWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val judulLomba = inputData.getString("judul_lomba") ?: return Result.failure()
        val sisaHari   = inputData.getInt("sisa_hari", 3)

        NotificationHelper.showDeadlineNotification(context, judulLomba, sisaHari)

        return Result.success()
    }
}