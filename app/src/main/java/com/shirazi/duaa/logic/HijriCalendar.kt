package com.shirazi.duaa.logic

import java.util.Calendar
import kotlin.math.floor

/** تحويل التقويم الهجري (خوارزمية تقويمية تقريبية) مع إمكان تعديل ±يومين. */
object HijriCalendar {

    val months = listOf(
        "مُحَرَّم", "صَفَر", "ربيع الأول", "ربيع الثاني", "جُمادى الأولى", "جُمادى الآخرة",
        "رَجَب", "شَعبان", "رَمَضان", "شَوّال", "ذو القَعدة", "ذو الحِجّة"
    )
    val weekDays = listOf("الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت")

    data class HDate(val y: Int, val m: Int, val d: Int)
    data class GDate(val y: Int, val m: Int, val d: Int)

    fun gToJd(y: Int, m: Int, d: Int): Long {
        val a = floor((14.0 - m) / 12).toLong()
        val yy = y + 4800 - a
        val mm = m + 12 * a - 3
        return d + floor((153.0 * mm + 2) / 5).toLong() + 365 * yy +
            floor(yy / 4.0).toLong() - floor(yy / 100.0).toLong() +
            floor(yy / 400.0).toLong() - 32045
    }

    fun jdToHijri(jdIn: Long, adj: Int = 0): HDate {
        val jd = jdIn + adj
        val l0 = jd - 1948440 + 10632
        val n = floor((l0 - 1) / 10631.0).toLong()
        var l = l0 - 10631 * n + 354
        val j = (floor((10985.0 - l) / 5316) * floor((50.0 * l) / 17719) +
            floor(l / 5670.0) * floor((43.0 * l) / 15238)).toLong()
        l = l - floor((30.0 - j) / 15).toLong() * floor((17719.0 * j) / 50).toLong() -
            floor(j / 16.0).toLong() * floor((15238.0 * j) / 43).toLong() + 29
        val m = floor((24.0 * l) / 709).toInt()
        val d = (l - floor((709.0 * m) / 24).toLong()).toInt()
        val y = (30 * n + j - 30).toInt()
        return HDate(y, m, d)
    }

    fun hijriToJd(y: Int, m: Int, d: Int, adj: Int = 0): Long {
        return floor((11.0 * y + 3) / 30).toLong() + 354L * y + 30L * m -
            floor((m - 1) / 2.0).toLong() + d + 1948440 - 385 - adj
    }

    fun jdToGreg(jdIn: Long): GDate {
        val a = jdIn + 32044
        val b = floor((4.0 * a + 3) / 146097).toLong()
        val c = a - floor((146097.0 * b) / 4).toLong()
        val dd = floor((4.0 * c + 3) / 1461).toLong()
        val e = c - floor((1461.0 * dd) / 4).toLong()
        val m = floor((5.0 * e + 2) / 153).toLong()
        val day = (e - floor((153.0 * m + 2) / 5).toLong() + 1).toInt()
        val month = (m + 3 - 12 * floor(m / 10.0).toLong()).toInt()
        val year = (100 * b + dd - 4800 + floor(m / 10.0).toLong()).toInt()
        return GDate(year, month, day)
    }

    fun today(adj: Int = 0): HDate {
        val n = Calendar.getInstance()
        return jdToHijri(gToJd(n.get(Calendar.YEAR), n.get(Calendar.MONTH) + 1, n.get(Calendar.DAY_OF_MONTH)), adj)
    }

    /** عدد أيام الشهر الهجري (29 أو 30). */
    fun monthLength(y: Int, m: Int, adj: Int = 0): Int {
        val jd1 = hijriToJd(y, m, 1, adj)
        val ny = if (m == 12) y + 1 else y
        val nm = if (m == 12) 1 else m + 1
        val jdNext = hijriToJd(ny, nm, 1, adj)
        val days = (jdNext - jd1).toInt()
        return days.coerceIn(29, 30)
    }
}
