package com.example.syndic.zaineb4.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.syndic.zaineb4.MainActivity
import com.example.syndic.zaineb4.R

object NotificationHelper {
    private const val CHANNEL_ID        = "announcements_channel"
    private const val CHANNEL_ID_EXP    = "expenses_channel"
    private const val NOTIFICATION_ID   = 1001
    private const val NOTIFICATION_ID_EXP = 1002

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel 1 – Announcements
            val annChannel = NotificationChannel(
                CHANNEL_ID,
                "إعلانات السانديك",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات الإعلانات الجديدة"
                enableVibration(true)
            }
            manager.createNotificationChannel(annChannel)

            // Channel 2 – Expenses
            val expChannel = NotificationChannel(
                CHANNEL_ID_EXP,
                "مصاريف السانديك",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "إشعارات عند إضافة مصروف جديد"
                enableVibration(true)
            }
            manager.createNotificationChannel(expChannel)
        }
    }

    /** Shown to all users when a new expense is added by admin */
    fun showNewExpenseNotification(context: Context, desc: String, amount: Double) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 2, intent, PendingIntent.FLAG_IMMUTABLE
        )
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_EXP)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("💸 مصروف جديد")
            .setContentText("$desc — ${"%.2f".format(amount)} DH")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("تمت إضافة مصروف جديد:\n📌 $desc\n💰 ${"%.2f".format(amount)} DH"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(500, 500))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_EXP, builder.build())
    }

    fun showNewAnnouncementNotification(context: Context, title: String, message: String) {
        // Intent to open the app when the notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app icon
            .setContentTitle("إعلان جديد: $title")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

