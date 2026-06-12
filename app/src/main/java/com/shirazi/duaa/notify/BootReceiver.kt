package com.shirazi.duaa.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** يعيد جدولة التنبيهات بعد إعادة تشغيل الجهاز. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            PrayerScheduler.reschedule(context)
        }
    }
}
