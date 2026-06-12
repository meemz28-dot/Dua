package com.shirazi.duaa.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.shirazi.duaa.MainActivity
import com.shirazi.duaa.R

/** يستقبل منبّه الوقت فيُظهر الإشعار ويعيد الجدولة. */
class PrayerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val name = intent.getStringExtra("name") ?: "الصلاة"
        showNotification(context, name)
        // إعادة الجدولة لليوم التالي
        PrayerScheduler.reschedule(context)
    }

    private fun showNotification(context: Context, prayerName: String) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "أوقات الصلاة", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "تنبيه عند دخول وقت الصلاة" }
            nm.createNotificationChannel(channel)
        }
        var flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            flags = flags or android.app.PendingIntent.FLAG_IMMUTABLE
        val openIntent = android.app.PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), flags
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("حان وقت صلاة $prayerName")
            .setContentText("اللهم صلِّ على محمد وآل محمد")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(openIntent)
            .build()
        nm.notify(prayerName.hashCode(), notif)
    }

    companion object { const val CHANNEL_ID = "prayer_times" }
}
