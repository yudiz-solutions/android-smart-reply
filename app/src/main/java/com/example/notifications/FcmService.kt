package com.example.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FcmService : FirebaseMessagingService() {
    private val CHANNEL_ID = "01"
    private val CHANNEL_NAME = "Chats"
    private val NOTIFICATION_ID = 101
    private val KEY_TEXT_REPLY = "key_text_reply"

    @RequiresApi(Build.VERSION_CODES.N)
    override fun handleIntent(intent: Intent) {
        this.onMessageReceived(RemoteMessage(intent.extras ?: Bundle()))
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.e(
            "TAG",
            "onMessageReceived title: ${remoteMessage.notification?.title}, body: ${remoteMessage.notification?.body} , Data: ${remoteMessage.data}"
        )

        showNotification(remoteMessage)

    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.e("TAG", "token: $token")
    }

    private fun showNotification(remoteMessage: RemoteMessage) {
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .build()

        val resultIntent = Intent(this, NotificationReceiver::class.java).putExtra(
            "timeStamp",
            System.currentTimeMillis()
        ).putExtra("name", remoteMessage.notification?.title)
            .putExtra("message", remoteMessage.notification?.body)
        val resultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getBroadcast(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "reply", resultPendingIntent
        )
            .addRemoteInput(remoteInput)
            .build()

        // Register the channel with the system
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "descriptionText"
            }
            notificationManager.createNotificationChannel(channel)
        }
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(
                NotificationCompat.MessagingStyle(Person.Builder().apply {
                    setName(remoteMessage.notification?.title?:"Test")
                }.build())
                    .addMessage(
                        remoteMessage.notification?.body.toString(),
                        System.currentTimeMillis(),
                        Person.Builder().apply {
                            setName(remoteMessage.notification?.title)
                        }.build()
                    )
            )
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .addAction(replyAction)
        with(NotificationManagerCompat.from(this)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}