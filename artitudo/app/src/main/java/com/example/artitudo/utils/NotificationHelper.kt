package com.example.artitudo.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.artitudo.MainActivity
import com.example.artitudo.R

class NotificationHelper(private val context: Context) {

    companion object {
        const val NEW_ELEMENT_CHANNEL_ID = "new_element_active_channel"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelName = context.getString(R.string.notification_channel_new_element_name)
            val channelDescription = context.getString(R.string.notification_channel_new_element_description)

            val newElementChannel = NotificationChannel(
                NEW_ELEMENT_CHANNEL_ID,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = channelDescription
            }
            notificationManager.createNotificationChannel(newElementChannel)
        }
    }

    fun showNewElementNotification(elementName: String, elementId: String, notificationId: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("elementId_from_notification", elementId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            pendingIntentFlags
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val contentTitle = context.getString(R.string.notification_title_new_element_added)
        val contentText = context.getString(R.string.notification_text_new_element_added)

        val notificationBuilder = NotificationCompat.Builder(context, NEW_ELEMENT_CHANNEL_ID)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(contentTitle)
            .setContentText("$contentText \"$elementName\"")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setSound(defaultSoundUri)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}