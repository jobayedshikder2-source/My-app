package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import java.util.concurrent.atomic.AtomicInteger

object NotificationHelper {
    const val CHANNEL_ID = "community_chat_channel"
    private const val CHANNEL_NAME = "কমিউনিটি বার্তা (Community Messages)"
    private const val CHANNEL_DESC = "WhatsApp এর মতো রিয়েল-টাইম বার্তা ও নোটিশের নোটিফিকেশন"

    private val notificationIdCounter = AtomicInteger(1001)

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                lightColor = 0xFF008069.toInt() // WhatsApp Emerald Green
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showMessageNotification(
        context: Context,
        senderName: String,
        messageText: String,
        groupTitle: String = "General Group Chat"
    ) {
        try {
            createNotificationChannel(context)

            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

            val displayContent = if (messageText.isBlank()) "📷 একটি ছবি পাঠিয়েছেন (Photo attached)" else messageText

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle(senderName)
                .setContentText(displayContent)
                .setSubText(groupTitle)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .setBigContentTitle(senderName)
                        .bigText(displayContent)
                        .setSummaryText(groupTitle)
                )
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(soundUri)
                .setVibrate(longArrayOf(0, 300, 150, 300))
                .setColor(0xFF008069.toInt())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

            val notificationId = notificationIdCounter.incrementAndGet()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        } catch (e: SecurityException) {
            Log.w("NotificationHelper", "Notification permission not granted: ${e.message}")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Failed to show notification", e)
        }
    }
}
