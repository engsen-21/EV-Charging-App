package smartcharge.master

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// Constants for notification channel ID and name
const val channelId = "notification_channel"
const val channelName = "smartcharge.master"
class MyFirebaseMessagingService : FirebaseMessagingService() {
    // Called when a message is received from Firebase Cloud Messaging
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains a notification payload
        if(remoteMessage.getNotification() != null){
            // Generate a notification with the message title and body
            generateNotification(remoteMessage.notification!!.title!!,remoteMessage.notification!!.body!!)
        }
    }
    // Create a custom remote view for the notification
    private fun getRemoteView(title: String, message: String): RemoteViews{
        // Initialize the RemoteViews object with the layout for the notification
        val remoteViews = RemoteViews("smartcharge.master",R.layout.activity_notification)

        // Set the title and message in the remote view
        remoteViews.setTextViewText(R.id.title, title)
        remoteViews.setTextViewText(R.id.message, message)

        // Set the app logo in the remote view
        remoteViews.setImageViewResource(R.id.app_logo, R.drawable.ic_smart_charge)
        return remoteViews
    }
    // Generate the notification
    private fun generateNotification(title: String, message: String){
        // Create an intent to launch the LoginActivity when the notification is clicked
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        // Create a pending intent to be triggered when the notification is clicked
        val pendingIntent = PendingIntent.getActivity(this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        // Initialize the NotificationCompat.Builder
        var builder: NotificationCompat.Builder =  NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_smart_charge) // Set the small icon for the notification
            .setAutoCancel(true) // Automatically remove the notification when clicked
            .setVibrate(longArrayOf(1000,1000,1000,1000)) // 1 Sec 1 Relax
            .setOnlyAlertOnce(true) // Alert only once for the notification
            .setContentIntent(pendingIntent)  // Set the pending intent to be triggered when clicked

        // Set the custom content for the notification
        builder = builder.setContent(getRemoteView(title,message))
        // Get the NotificationManager system service
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // For Android Oreo and above, create a notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            // Create a NotificationChannel object
            // Parameters:
            // - channelId: A unique ID for this notification channel. Used to reference the channel when sending notifications.
            // - channelName: A user-visible name for the channel. This can be seen in the system settings.
            // - importance: The importance level for notifications posted to this channel. This controls the interruptiveness of notifications.
            val notificationChannel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
            // Register the channel with the system
            // This is necessary to enable the system to manage this channel's settings.
            notificationManager.createNotificationChannel(notificationChannel)
        }

        // Notify the notification manager to show the notification
        notificationManager.notify(0,builder.build())
    }
}