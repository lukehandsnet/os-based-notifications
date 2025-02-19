package com.example.osnotifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import androidx.core.content.ContextCompat
import com.example.osnotifications.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val CHANNEL_ID = "default"
    private var notificationId = 0
    private val GROUP_KEY = "com.example.osnotifications.NOTIFICATION_GROUP"
    private val KEY_TEXT_REPLY = "key_text_reply"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showSimpleNotification()
        } else {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.apply {
            btnSimpleNotification.setOnClickListener { checkPermissionAndShow { showSimpleNotification() } }
            btnBigTextNotification.setOnClickListener { checkPermissionAndShow { showBigTextNotification() } }
            btnActionNotification.setOnClickListener { checkPermissionAndShow { showActionNotification() } }
            btnProgressNotification.setOnClickListener { checkPermissionAndShow { showProgressNotification() } }
            btnUrgentNotification.setOnClickListener { checkPermissionAndShow { showUrgentNotification() } }
            btnGroupedNotifications.setOnClickListener { checkPermissionAndShow { showGroupedNotifications() } }
            btnReplyNotification.setOnClickListener { checkPermissionAndShow { showReplyNotification() } }
        }
    }

    private fun checkPermissionAndShow(showNotification: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    showNotification()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            showNotification()
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
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showSimpleNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Simple Notification")
            .setContentText("This is a basic notification")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        showNotification(builder.build())
    }

    private fun showBigTextNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Big Text Notification")
            .setContentText("This is a notification with expanded text")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("This is a much longer text that will be displayed when the notification is expanded. It can contain multiple lines of text and will automatically wrap to show all the content."))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        showNotification(builder.build())
    }

    private fun showActionNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Action Notification")
            .setContentText("This notification has action buttons")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_view, "Open App", pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Dismiss", null)

        showNotification(builder.build())
    }

    private fun showProgressNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Download Progress")
            .setContentText("Download in progress")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)

        CoroutineScope(Dispatchers.Main).launch {
            for (progress in 0..100 step 10) {
                builder.setProgress(100, progress, false)
                builder.setContentText("Download in progress: $progress%")
                showNotification(builder.build())
                delay(500)
            }
            builder.setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
            showNotification(builder.build())
        }
    }

    private fun showUrgentNotification() {
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Urgent Notification")
            .setContentText("This is a high-priority notification!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.RED)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setAutoCancel(true)

        showNotification(builder.build())
    }

    private fun showGroupedNotifications() {
        // Create group summary notification
        val summaryNotification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("2 New Messages")
            .setContentText("New messages from Alice and Bob")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setGroup(GROUP_KEY)
            .setGroupSummary(true)
            .build()

        // Create individual notifications
        val notification1 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Alice")
            .setContentText("Hey, how are you?")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setGroup(GROUP_KEY)
            .build()

        val notification2 = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bob")
            .setContentText("Meeting at 3 PM")
            .setSmallIcon(android.R.drawable.ic_dialog_email)
            .setGroup(GROUP_KEY)
            .build()

        try {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId++, notification1)
                notify(notificationId++, notification2)
                notify(0, summaryNotification)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showReplyNotification() {
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
            .setContentTitle("Direct Reply Notification")
            .setContentText("Tap reply to respond")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .addAction(action)

        showNotification(builder.build())
    }

    private fun showNotification(notification: android.app.Notification) {
        try {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId++, notification)
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}