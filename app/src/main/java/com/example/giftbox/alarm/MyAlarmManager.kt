package com.example.giftbox.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.giftbox.model.Gift
import java.util.Calendar

class MyAlarmManager(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(gift: Gift, dDay: Int) {
        for (i in 0..dDay) {
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("gift", gift)
                putExtra("dDay", dDay)
            }
            val year = gift.endDt.substring(0, 4)
            val month = gift.endDt.substring(4, 6)
            val day = gift.endDt.substring(6, 8)
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year.toInt())
                set(Calendar.MONTH, month.toInt() - 1) // 0이 1월
                set(Calendar.DATE, day.toInt() - i)
                set(Calendar.HOUR_OF_DAY, 0) // 0
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                PendingIntent.getBroadcast(
                    context,
                    gift.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        }
    }

    fun cancel(id: String) {
        alarmManager.cancel(
            PendingIntent.getBroadcast(
                context,
                id.hashCode(),
                Intent(context, AlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
    }

}