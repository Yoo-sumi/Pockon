package com.example.giftbox.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.giftbox.MainActivity
import com.example.giftbox.MainApplication.Companion.CHANNEL_ID
import com.example.giftbox.R
import com.example.giftbox.data.GiftRepository
import com.example.giftbox.model.Gift
import com.example.giftbox.ui.utils.getDdayInt
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var giftRepository: GiftRepository
    @Inject
    lateinit var sharedPref: SharedPreferences
    @Inject
    lateinit var myAlarmManager: MyAlarmManager

    override fun onReceive(context: Context, intent: Intent) {
        // 재부팅 후 알람 매니저 재등록
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            CoroutineScope(Dispatchers.IO).launch {
                val isNotiEndDt = sharedPref.getBoolean("noti_end_dt", true)
                giftRepository.getAllGift().take(1).collectLatest { allGift ->
                    allGift.forEach { gift ->
                        val tempGift = Gift(id = gift.id, uid = gift.uid, photo = gift.photo, name = gift.name, brand = gift.brand, endDt = gift.endDt, addDt = gift.addDt, memo = gift.memo, usedDt = gift.usedDt)
                        myAlarmManager.cancel(tempGift.id)
                        // 알림 등록
                        if (isNotiEndDt && getDdayInt(tempGift.endDt) in 0..1) {
                            myAlarmManager.schedule(tempGift, getDdayInt(tempGift.endDt))
                        }
                    }
                    sharedPref.edit().putBoolean("noti_register", true).apply()
                }
            }
        } else { // 등록된 알람 수신
            val gift = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra("gift", Gift::class.java)
            } else {
                intent.getSerializableExtra("gift") as Gift
            }
            val dDay = intent.getIntExtra("dDay", 0)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val myIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent =
                PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_IMMUTABLE)

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
                notificationBuilder.setContentText(
                    context.getString(
                        R.string.msg_noti_end_dt,
                        dDay
                    )
                )
            }

            notificationManager.notify(
                "${gift?.id}${getDdayInt(gift?.endDt ?: "0")}".hashCode(),
                notificationBuilder.build()
            )
        }
    }
}