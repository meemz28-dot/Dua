package com.shirazi.duaa.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.shirazi.duaa.data.Settings
import com.shirazi.duaa.logic.PrayerTimes
import java.util.Calendar

/** جدولة تنبيهات أوقات الصلاة عبر AlarmManager. */
object PrayerScheduler {

    private val notifySlots = listOf("fajr", "dhuhr", "asr", "maghrib", "isha")
    private val slotNames = mapOf(
        "fajr" to "الفجر", "dhuhr" to "الظهر", "asr" to "العصر",
        "maghrib" to "المغرب", "isha" to "العشاء"
    )

    fun reschedule(context: Context) {
        val settings = Settings(context)
        cancelAll(context)
        if (!settings.notifyEnabled.value) return

        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val loc = settings.location()
        val now = Calendar.getInstance()

        // schedule for today and tomorrow to cover rollover
        for (dayOffset in 0..1) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dayOffset) }
            val t = PrayerTimes.compute(cal, loc.lat, loc.lng, loc.tz, settings.method.value)
            notifySlots.forEachIndexed { i, slot ->
                val hours = PrayerTimes.norm(PrayerTimes.value(t, slot))
                val h = hours.toInt()
                val min = Math.round((hours - h) * 60).toInt()
                val fire = (cal.clone() as Calendar).apply {
                    set(Calendar.HOUR_OF_DAY, h.coerceIn(0, 23))
                    set(Calendar.MINUTE, min.coerceIn(0, 59))
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (fire.timeInMillis <= now.timeInMillis) return@forEachIndexed
                val reqId = dayOffset * 100 + i
                val pi = pendingIntent(context, reqId, slot, slotNames[slot] ?: slot)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
                        am.setWindow(AlarmManager.RTC_WAKEUP, fire.timeInMillis, 60_000L, pi)
                    } else {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, fire.timeInMillis, pi)
                    }
                } catch (e: SecurityException) {
                    am.set(AlarmManager.RTC_WAKEUP, fire.timeInMillis, pi)
                }
            }
        }
    }

    fun cancelAll(context: Context) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        for (dayOffset in 0..1) for (i in notifySlots.indices) {
            val reqId = dayOffset * 100 + i
            am.cancel(pendingIntent(context, reqId, "", ""))
        }
    }

    private fun pendingIntent(context: Context, reqId: Int, slot: String, name: String): PendingIntent {
        val intent = Intent(context, PrayerReceiver::class.java).apply {
            action = "com.shirazi.duaa.PRAYER_$slot"
            putExtra("slot", slot)
            putExtra("name", name)
        }
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, reqId, intent, flags)
    }
}
