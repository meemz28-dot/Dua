package com.shirazi.duaa.logic

import java.util.Calendar
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** حساب مواقيت الصلاة فلكياً (أسلوب PrayTimes) مع منتصف الليل الشرعي وطرق متعددة. */
object PrayerTimes {

    data class Method(
        val key: String, val name: String,
        val fajr: Double, val maghrib: Double?, // null => sunset based; otherwise depression deg
        val isha: Double, val ishaMin: Boolean,  // ishaMin=true => isha = maghrib + 90min
        val jafariMidnight: Boolean
    )

    val methods = listOf(
        Method("jafari", "الجعفري (ليفا - قم)", 16.0, 4.0, 14.0, false, true),
        Method("tehran", "طهران", 17.7, 4.5, 14.0, false, true),
        Method("karachi", "كراتشي", 18.0, null, 18.0, false, false),
        Method("mwl", "رابطة العالم الإسلامي", 18.0, null, 17.0, false, false),
        Method("makkah", "أم القرى (مكة)", 18.5, null, 0.0, true, false)
    )
    fun methodByKey(k: String) = methods.firstOrNull { it.key == k } ?: methods[0]

    data class Times(
        val fajr: Double, val sunrise: Double, val dhuhr: Double, val asr: Double,
        val maghrib: Double, val isha: Double, val midnight: Double
    )

    private fun rtd(r: Double) = r * 180.0 / Math.PI
    private fun dtr(d: Double) = d * Math.PI / 180.0

    private data class Sun(val decl: Double, val eqt: Double)
    private fun sunPos(jd: Double): Sun {
        val d = jd - 2451545.0
        val g = (357.529 + 0.98560028 * d) % 360
        val q = (280.459 + 0.98564736 * d) % 360
        val l = (q + 1.915 * sin(dtr(g)) + 0.020 * sin(dtr(2 * g))) % 360
        val e = 23.439 - 0.00000036 * d
        val decl = rtd(asin(sin(dtr(e)) * sin(dtr(l))))
        var ra = rtd(atan2(cos(dtr(e)) * sin(dtr(l)), cos(dtr(l)))) / 15.0
        ra = (ra % 24 + 24) % 24
        return Sun(decl, q / 15.0 - ra)
    }

    /** زاوية الساعة لانخفاض الشمس بمقدار angle تحت الأفق (بالساعات). null إن لم تتحقق. */
    private fun tAngle(angle: Double, lat: Double, decl: Double): Double? {
        val x = (-sin(dtr(angle)) - sin(dtr(lat)) * sin(dtr(decl))) /
            (cos(dtr(lat)) * cos(dtr(decl)))
        if (x < -1 || x > 1) return null
        return rtd(acos(x)) / 15.0
    }

    /** زاوية ساعة العصر (ظل المثل، factor=1). */
    private fun asrAngle(lat: Double, decl: Double): Double? {
        val angle = rtd(atan(1.0 / (1.0 + tan(abs(dtr(lat - decl))))))
        val x = (sin(dtr(angle)) - sin(dtr(lat)) * sin(dtr(decl))) /
            (cos(dtr(lat)) * cos(dtr(decl)))
        if (x < -1 || x > 1) return null
        return rtd(acos(x)) / 15.0
    }

    fun compute(cal: Calendar, lat: Double, lng: Double, tz: Double, methodKey: String): Times {
        val m = methodByKey(methodKey)
        val jd = HijriCalendar.gToJd(
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH)
        ).toDouble() - lng / (15.0 * 24.0)
        val sp = sunPos(jd)
        val decl = sp.decl
        val noon = 12.0 - sp.eqt - lng / 15.0 + tz
        val hr = tAngle(0.833, lat, decl)
        val ha = hr ?: 6.0
        val sunrise = noon - ha
        val sunset = noon + ha
        val night = 24.0 - 2 * ha

        val fa = tAngle(m.fajr, lat, decl)
        val fajr = if (fa != null) noon - fa else sunrise - night / 7.0

        val maghrib: Double = if (m.maghrib != null) {
            val mg = tAngle(m.maghrib, lat, decl)
            if (mg != null) noon + mg else sunset + night / 12.0
        } else sunset

        val isha: Double = if (m.ishaMin) {
            maghrib + 1.5
        } else {
            val ia = tAngle(m.isha, lat, decl)
            if (ia != null) noon + ia else sunset + night / 7.0
        }

        val asr = noon + (asrAngle(lat, decl) ?: 0.0)

        val midnight: Double = if (m.jafariMidnight) {
            val span = (fajr + 24) - maghrib
            (maghrib + span / 2.0) % 24
        } else {
            val span = (sunrise + 24) - sunset
            (sunset + span / 2.0) % 24
        }
        return Times(fajr, sunrise, noon, asr, maghrib, isha, midnight)
    }

    fun norm(t: Double): Double = ((t % 24) + 24) % 24

    fun fmt(tIn: Double): String {
        if (tIn.isNaN()) return "—"
        var t = norm(tIn)
        var h = floorInt(t)
        var min = Math.round((t - h) * 60).toInt()
        if (min == 60) { min = 0; h++ }
        if (h >= 24) h -= 24
        val am = h < 12
        var hh = h % 12
        if (hh == 0) hh = 12
        val mm = if (min < 10) "0$min" else "$min"
        return "$hh:$mm ${if (am) "ص" else "م"}"
    }
    private fun floorInt(d: Double) = kotlin.math.floor(d).toInt()

    data class Slot(val key: String, val name: String)
    val slots = listOf(
        Slot("fajr", "الفجر"), Slot("sunrise", "الشروق"), Slot("dhuhr", "الظهر"),
        Slot("asr", "العصر"), Slot("maghrib", "المغرب"), Slot("isha", "العشاء"),
        Slot("midnight", "منتصف الليل")
    )
    fun value(t: Times, key: String): Double = when (key) {
        "fajr" -> t.fajr; "sunrise" -> t.sunrise; "dhuhr" -> t.dhuhr; "asr" -> t.asr
        "maghrib" -> t.maghrib; "isha" -> t.isha; "midnight" -> t.midnight; else -> Double.NaN
    }

    /** الصلاة القادمة الآن: المفتاح وعدد الساعات المتبقية. */
    fun next(t: Times, nowHours: Double): Pair<String, Double> {
        var k = "fajr"; var best = Double.MAX_VALUE
        for (s in slots) {
            if (s.key == "sunrise" || s.key == "midnight") continue
            var dt = norm(value(t, s.key)) - nowHours
            if (dt < 0) dt += 24
            if (dt < best) { best = dt; k = s.key }
        }
        return k to best
    }
}
