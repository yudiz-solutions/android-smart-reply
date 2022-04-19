package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person


class NotificationReceiver : BroadcastReceiver() {
    private val CHANNEL_ID = "01"
    private val NOTIFICATION_ID = 101
    private val KEY_TEXT_REPLY = "key_text_reply"
    override fun onReceive(context: Context, intent: Intent) {
        val remoteInput = android.app.RemoteInput.getResultsFromIntent(intent)
        val message: String = remoteInput.getString(KEY_TEXT_REPLY).toString()
        if (remoteInput != null) {
            /*
            ViewModel code for calling your API
            vm.sendMessage(message)
            fun onSuccess(){
            showNotification(context,message)
            }
             */
            val time = intent.getLongExtra("timeStamp", 0L)
            val name = intent.getStringExtra("name")
            val msg = intent.getStringExtra("message")
            Log.e("TAG", "onReceive: $name")
            val remote = NotificationCompat.MessagingStyle.Message(
                msg,
                time,
                Person.Builder().apply {
                    setName(name)
                }.build()
            )
            showNotification(context, message, remote)
        }
    }

    private fun showNotification(
        context: Context,
        message: String,
        msg: NotificationCompat.MessagingStyle.Message
    ) {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setStyle(
                NotificationCompat.MessagingStyle(Person.Builder().apply {
                    setName("You")
                }.build())
                    .addMessage(msg)
                    .addMessage(
                        NotificationCompat.MessagingStyle.Message(
                            message,
                            System.currentTimeMillis(),
                            Person.Builder().apply {
                                setName("You")
                            }.build()
                        )
                    )
            )
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
