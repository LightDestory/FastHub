package com.fastaccess.provider.fcm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import com.fastaccess.R
import com.fastaccess.data.dao.model.FastHubNotification
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMain
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONObject
import java.util.*

/**
 * Created by Kosh on 16 Apr 2017, 1:17 PM
 */
@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class PushNotificationService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        remoteMessage.data
        if (remoteMessage.data.isNotEmpty()) {
            val date = Date(remoteMessage.sentTime)
            val fastHubNotification = RestProvider.gson
                .fromJson(
                    JSONObject(remoteMessage.data as Map<*, *>?).toString(),
                    FastHubNotification::class.java
                )
            fastHubNotification.date = date
            FastHubNotification.save(fastHubNotification)
        } else if (remoteMessage.notification != null) {
            var title = remoteMessage.notification!!.title
            var body = remoteMessage.notification!!.body
            remoteMessage.data
            if (remoteMessage.data.isNotEmpty()) {
                title = title ?: remoteMessage.data["title"]
                body = body ?: remoteMessage.data["message"]
            }
            val intent = launchMain(this, true)
            @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent =
                PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
            val notificationBuilder = NotificationCompat.Builder(this, "In App-Notifications")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationBuilder.build())
        }
    }
}