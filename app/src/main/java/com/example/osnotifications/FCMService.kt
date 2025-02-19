package com.example.osnotifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {
    companion object {
        private const val CHANNEL_ID = "fcm_default_channel"
        private const val KEY_TEXT_REPLY = "key_text_reply"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this token to your server
        println("New FCM token: $token")
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationType = message.data["type"] ?: "simple"
        
        when (notificationType) {
            "simple" -> showSimpleNotification(message)
            "bigText" -> showBigTextNotification(message)
            "action" -> showActionNotification(message)
            "progress" -> showProgressNotification(message)
            "urgent" -> showUrgentNotification(message)
            "reply" -> showReplyNotification(message)
            else -> showSimpleNotification(message)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Default Channel"
            val descriptionText = "Default notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSimpleNotification(message: RemoteMessage) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Notification")
            .setContentText(message.notification?.body ?: "Message received")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.primary))

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showBigTextNotification(message: RemoteMessage) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Big Text Notification")
            .setContentText(message.notification?.body ?: "Expandable message")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(message.data["big_text"] ?: message.notification?.body ?: "Expanded text"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.primary))

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showActionNotification(message: RemoteMessage) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(message.notification?.title ?: "Action Notification")
            .setContentText(message.notification?.body ?: "This notification has actions")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.primary))
            .addAction(android.R.drawable.ic_menu_view, "Open App", pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", null)

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showProgressNotification(message: RemoteMessage) {
        val progress = message.data["progress"]?.toIntOrNull() ?: -1
        val isIndeterminate = progress == -1

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(message.notification?.title ?: "Download Progress")
            .setContentText(if (isIndeterminate) "Download in progress" else "Progress: $progress%")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setProgress(100, progress, isIndeterminate)
            .setColor(ContextCompat.getColor(this, R.color.primary))

        try {
            NotificationManagerCompat.from(this)
                .notify(message.data["notification_id"]?.toIntOrNull() ?: System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showUrgentNotification(message: RemoteMessage) {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(message.notification?.title ?: "Urgent!")
            .setContentText(message.notification?.body ?: "This is urgent!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(ContextCompat.getColor(this, R.color.accent))
            .setVibrate(longArrayOf(0, 500))
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun showReplyNotification(message: RemoteMessage) {
        val replyLabel = "Enter your reply"
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel(replyLabel)
            .build()

        val intent = Intent(this, MainActivity::class.java)
        val replyPendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_MUTABLE
        )

        val action = NotificationCompat.Action.Builder(
            android.R.drawable.ic_menu_send,
            "Reply",
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setContentTitle(message.notification?.title ?: "Reply Notification")
            .setContentText(message.notification?.body ?: "Tap reply to respond")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setColor(ContextCompat.getColor(this, R.color.primary))
            .addAction(action)

        try {
            NotificationManagerCompat.from(this)
                .notify(System.currentTimeMillis().toInt(), builder.build())
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }
}