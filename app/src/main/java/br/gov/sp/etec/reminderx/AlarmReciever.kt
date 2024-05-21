package br.gov.sp.etec.reminderx

import android.R
import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        // Verifica se o contexto e o intent não são nulos
        if (context != null && intent != null) {
            // Obtém a mensagem do lembrete do intent
            val message = intent.getStringExtra("message")

            // Cria uma notificação
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channelId = "reminder_channel"
//            val channelName = "Reminder Channel"
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
//                notificationManager.createNotificationChannel(channel)
//            }

            val vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            val mBuilder: NotificationCompat.Builder?

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val notificationChannel = NotificationChannel(channelId, message, importance)
                notificationChannel.enableLights(true)
                notificationChannel.enableVibration(true)
                notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
                notificationChannel.vibrationPattern = vibrationPattern
                notificationManager.createNotificationChannel(notificationChannel)

                mBuilder = buildNotification(
                    context,
                    message,
                    "Lembrete",
                    R.drawable.ic_notification_overlay,
                    channelId,
                    vibrationPattern
                )
                mBuilder!!.setChannelId(channelId)
            } else {
                mBuilder = NotificationCompat.Builder(context, channelId)
                mBuilder.setSmallIcon(R.drawable.ic_notification_overlay)
                mBuilder.setContentTitle("Lembrete")
                    .setContentText(message)
                    .setAutoCancel(false)
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setVibrate(vibrationPattern)
            }
            notificationManager.notify(0, mBuilder.build())
        }
    }
}

@TargetApi(Build.VERSION_CODES.O)
fun buildNotification(
    mContext: Context,
    text: String?,
    fromnumber: String?,
    icon: Int,
    channelId: String?,
    vibrationPattern: LongArray?
): NotificationCompat.Builder? {
    return NotificationCompat.Builder(mContext, channelId!!)
        .setSmallIcon(icon)
        .setContentTitle(fromnumber)
        .setSubText("ReminderX")
        .setContentText(text)
        .setAutoCancel(true)
        .setOngoing(true)
        .setAutoCancel(true)
        .setVibrate(vibrationPattern)
}