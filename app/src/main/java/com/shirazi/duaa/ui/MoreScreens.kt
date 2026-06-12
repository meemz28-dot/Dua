package com.shirazi.duaa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import com.shirazi.duaa.data.Data
import com.shirazi.duaa.data.Settings
import com.shirazi.duaa.logic.HijriCalendar
import com.shirazi.duaa.logic.PrayerTimes
import com.shirazi.duaa.notify.PrayerScheduler
import java.util.Calendar

// ============ الأوقات ============
@Composable
fun PrayerScreen(settings: Settings, pal: DuaaPalette, openSheet: (String) -> Unit) {
    val now = Calendar.getInstance()
    val loc = settings.location()
    val times = PrayerTimes.compute(now, loc.lat, loc.lng, loc.tz, settings.method.value)
    val nowH = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0
    val (nextKey, dt) = PrayerTimes.next(times, nowH)
    val nextName = PrayerTimes.slots.firstOrNull { it.key == nextKey }?.name ?: ""
    val hh = dt.toInt(); val mm = Math.round((dt - hh) * 60).toInt()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ArchCard(pal) {
            Text("الصلاة القادمة", color = pal.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(nextName, color = pal.emerald, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
            Text("${PrayerTimes.fmt(PrayerTimes.value(times, nextKey))} — بعد ${hh}س ${mm}د", color = pal.ink2, fontSize = 13.sp)
        }
        PlainCard(pal) {
            Text("📍 ${loc.name}", color = pal.ink2, fontSize = 13.sp, modifier = Modifier.padding(bottom = 6.dp))
            PrayerTimes.slots.forEach { s ->
                val isNext = s.key == nextKey
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (isNext) pal.gold.copy(alpha = .10f) else Color.Transparent)
                        .padding(horizontal = 8.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(s.name, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(PrayerTimes.fmt(PrayerTimes.value(times, s.key)), fontWeight = FontWeight.ExtraBold, color = pal.emerald)
                }
            }
        }
        BigButton("📍 تغيير الموقع", pal, false) { openSheet("loc") }
        Spacer(Modifier.height(10.dp))
        BigButton("⚙ طريقة الحساب: ${PrayerTimes.methodByKey(settings.method.value).name}", pal, true) { openSheet("method") }
        Spacer(Modifier.height(14.dp))
        Text("الأوقات محسوبة فلكياً بحسب موقعك وتقريبية، وقد تختلف دقائق عن التقويم المحلي. يُحتاط في أوّل الوقت وآخره.",
            color = pal.ink2, fontSize = 12.sp, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}

@Composable
fun BigButton(label: String, pal: DuaaPalette, gold: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(if (gold) pal.gold else pal.emerald).clickable(onClick = onClick).padding(15.dp),
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp) }
}

// ============ التقويم ============
@Composable
fun CalendarScreen(settings: Settings, pal: DuaaPalette) {
    val today = HijriCalendar.today(settings.hijriAdj.value)
    var calY by remember { mutableStateOf(today.y) }
    var calM by remember { mutableStateOf(today.m) }
    var selDay by remember { mutableStateOf(if (calY == today.y && calM == today.m) today.d else 1) }

    val adj = settings.hijriAdj.value
    val jd1 = HijriCalendar.hijriToJd(calY, calM, 1, adj)
    val g = HijriCalendar.jdToGreg(jd1)
    val dow = run {
        val c = Calendar.getInstance(); c.set(g.y, g.m - 1, g.d); c.get(Calendar.DAY_OF_WEEK) - 1
    }
    val days = HijriCalendar.monthLength(calY, calM, adj)

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            CalNavBtn("›", pal) { calM--; if (calM < 1) { calM = 12; calY-- }; selDay = 1 }
            Text("${HijriCalendar.months[calM - 1]} $calY هـ", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
            CalNavBtn("‹", pal) { calM++; if (calM > 12) { calM = 1; calY++ }; selDay = 1 }
        }
        // أيام الأسبوع + الشبكة
        val cells = ArrayList<Int?>()
        repeat(dow) { cells.add(null) }
        for (d in 1..days) cells.add(d)
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(((cells.size / 7 + 2) * 48).dp)) {
            items(HijriCalendar.weekDays) { wd -> Box(Modifier.padding(4.dp), contentAlignment = Alignment.Center) { Text(wd.take(3), color = pal.ink2, fontSize = 11.sp, fontWeight = FontWeight.Bold) } }
            items(cells) { d ->
                if (d == null) Box(Modifier.padding(2.dp).aspectRatio(1f)) {}
                else {
                    val occ = Data.occasions.filter { it.m == calM && it.d == d }
                    val isToday = calY == today.y && calM == today.m && d == today.d
                    val isSel = d == selDay
                    Box(
                        Modifier.padding(2.dp).aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                            .background(if (isToday) pal.emerald else MaterialTheme.colorScheme.surface)
                            .border(if (isSel && !isToday) 1.5.dp else 1.dp, if (isSel) pal.gold else pal.line, RoundedCornerShape(12.dp))
                            .clickable { selDay = d },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$d", color = if (isToday) Color.White else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        if (occ.isNotEmpty()) {
                            val dotColor = when (occ[0].k) { "eid" -> pal.good; "birth" -> pal.gold; "death" -> pal.danger; else -> pal.ink2 }
                            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 5.dp).size(6.dp).clip(CircleShape).background(dotColor))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        // مناسبات اليوم المختار
        val occ = Data.occasions.filter { it.m == calM && it.d == selDay }
        val gg = HijriCalendar.jdToGreg(HijriCalendar.hijriToJd(calY, calM, selDay, adj))
        PlainCard(pal) {
            Text("$selDay ${HijriCalendar.months[calM - 1]} $calY هـ — ${gg.d}/${gg.m}/${gg.y}م",
                fontWeight = FontWeight.ExtraBold, color = pal.emerald, fontSize = 15.sp, modifier = Modifier.padding(bottom = 8.dp))
            if (occ.isEmpty()) Text("لا توجد مناسبة مسجّلة في هذا اليوم.", color = pal.ink2)
            else occ.forEach { o ->
                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    val (bg, fg) = when (o.k) {
                        "eid" -> pal.good.copy(alpha = .18f) to pal.good
                        "birth" -> pal.gold.copy(alpha = .18f) to pal.gold
                        "death" -> pal.danger.copy(alpha = .15f) to pal.danger
                        else -> pal.surface2 to pal.ink2
                    }
                    val tag = when (o.k) { "eid" -> "عيد"; "birth" -> "ولادة"; "death" -> "وفاة"; else -> "مناسبة" }
                    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text(tag, color = fg, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(o.t, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                }
            }
        }
        // مفتاح الألوان
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            LegendDot("عيد", pal.good); Spacer(Modifier.width(14.dp))
            LegendDot("ولادة", pal.gold); Spacer(Modifier.width(14.dp))
            LegendDot("وفاة", pal.danger)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color)); Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable private fun CalNavBtn(label: String, pal: DuaaPalette, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface)
        .border(1.dp, pal.line, RoundedCornerShape(10.dp)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

// ============ المزيد ============
@Composable
fun MoreScreen(pal: DuaaPalette, openSheet: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        PlainCard(pal) {
            MoreRow("✦", "خِيرة القرآن", pal) { openSheet("khira") }
            MoreRow("📿", "تسبيح الزهراء", pal) { openSheet("tasbih") }
            MoreRow("★", "المفضلة", pal) { openSheet("fav") }
            MoreRow("⚙️", "الإعدادات", pal) { openSheet("settings") }
            MoreRow("ℹ️", "حول الكتاب والتطبيق", pal, last = true) { openSheet("about") }
        }
    }
}

@Composable private fun MoreRow(icon: String, label: String, pal: DuaaPalette, last: Boolean = false, onClick: () -> Unit) {
    Column {
        Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 20.sp, modifier = Modifier.width(34.dp), textAlign = TextAlign.Center)
            Spacer(Modifier.width(12.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
            Text("‹", color = pal.ink2)
        }
        if (!last) Divider(color = pal.line)
    }
}
