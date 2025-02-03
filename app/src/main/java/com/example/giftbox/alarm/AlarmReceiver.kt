package com.example.giftbox.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.giftbox.MainActivity
import com.example.giftbox.MainApplication.Companion.CHANNEL_ID
import com.example.giftbox.R
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.getDdayInt

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val gift = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("gift",Gift::class.java)
        } else {
            intent.getSerializableExtra("gift") as Gift
        }
        val dDay = intent.getIntExtra("dDay", 0)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val myIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("${gift?.brand} ${gift?.name}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if (dDay == 0) {
            notificationBuilder.setContentText(context.getString(R.string.msg_noti_end_dt_today))
        } else {
            notificationBuilder.setContentText(context.getString(R.string.msg_noti_end_dt, dDay))
        }

        notificationManager.notify("${gift?.id}${getDdayInt(gift?.endDt ?: "0")}".hashCode(), notificationBuilder.build())
    }

}