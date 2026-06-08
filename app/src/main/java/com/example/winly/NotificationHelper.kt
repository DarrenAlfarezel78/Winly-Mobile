package com.example.winly

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "winly_deadline"
    private const val CHANNEL_NAME = "Deadline Lomba"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifikasi pengingat deadline lomba"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showDeadlineNotification(context: Context, judulLomba: String, sisaHari: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Deadline Lomba H-3!")
            .setContentText("\"$judulLomba\" akan ditutup dalam $sisaHari hari lagi!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                .bigText("Jangan sampai ketinggalan! Lomba \"$judulLomba\" akan ditutup dalam $sisaHari hari lagi. Segera daftar sekarang!"))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(judulLomba.hashCode(), notification)
    }
}