package com.example.winly

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.winly.DeadlineWorker
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object DeadlineScheduler {

    fun scheduleDeadlineNotification(
        context: Context,
        competitionId: Int,
        judulLomba: String,
        tanggalDeadline: String?
    ) {
        if (tanggalDeadline.isNullOrEmpty()) return

        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val deadline = format.parse(tanggalDeadline) ?: return

            // Hitung waktu notifikasi = deadline - 3 hari
            val notifTime = deadline.time - (3 * 24 * 60 * 60 * 1000L)
            val delay = notifTime - System.currentTimeMillis()

            // Kalau deadline sudah lewat atau kurang dari 3 hari, skip
            if (delay <= 0) return

            val sisaHari = (deadline.time - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)

            val inputData = workDataOf(
                "judul_lomba" to judulLomba,
                "sisa_hari" to sisaHari.toInt()
            )

            val workRequest = OneTimeWorkRequestBuilder<DeadlineWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag("deadline_$competitionId")
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "deadline_$competitionId",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelDeadlineNotification(context: Context, competitionId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork("deadline_$competitionId")
    }
}