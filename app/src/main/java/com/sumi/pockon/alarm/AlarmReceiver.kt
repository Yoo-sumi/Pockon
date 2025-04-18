package com.sumi.pockon.alarm

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import com.sumi.pockon.ui.main.MainActivity
import com.sumi.pockon.MainApplication.Companion.CHANNEL_ID
import com.sumi.pockon.MainApplication.Companion.GROUP_KEY
import com.sumi.pockon.R
import com.sumi.pockon.data.repository.GiftRepository
import com.sumi.pockon.data.model.Gift
import com.sumi.pockon.util.getDdayInt
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
                val notiEndDay = sharedPref.getInt("noti_end_dt_day", 0)
                sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
                val alarmList = mutableSetOf<String>()
                giftRepository.getAllGift().take(1).collectLatest { allGift ->
                    allGift.forEach { gift ->
                        val tempGift = Gift(
                            id = gift.id,
                            uid = gift.uid,
                            name = gift.name,
                            brand = gift.brand,
                            endDt = gift.endDt,
                            addDt = gift.addDt,
                            memo = gift.memo,
                            usedDt = gift.usedDt,
                            cash = gift.cash,
                            isFavorite = gift.isFavorite
                        )
                        // 알림 등록
                        if (isNotiEndDt && getDdayInt(tempGift.endDt) == notiEndDay) {
                            alarmList.add(gift.id)
                            myAlarmManager.schedule(tempGift, getDdayInt(tempGift.endDt))
                        } else {
                            myAlarmManager.cancel(tempGift.id)
                        }
                    }
                    if (isNotiEndDt) {
                        sharedPref.edit().putStringSet("alarm_list", alarmList).apply()
                    } else {
                        sharedPref.edit().putStringSet("alarm_list", mutableSetOf()).apply()
                    }
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
                .setSmallIcon(R.drawable.ic_noti_gift)
                .setContentTitle("${gift?.brand} ${gift?.name}")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setGroup(GROUP_KEY) // 그룹 키 지정
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

            // 그룹 요약 알림
            val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_noti_gift)
                .setStyle(NotificationCompat.InboxStyle())
                .setGroup(GROUP_KEY)
                .setGroupSummary(true)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(gift?.id.hashCode(), summaryNotification)
        }
    }
}