package com.shirazi.duaa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shirazi.duaa.data.Data
import com.shirazi.duaa.data.Settings
import com.shirazi.duaa.logic.ArabicSearch
import com.shirazi.duaa.logic.HijriCalendar
import com.shirazi.duaa.logic.PrayerTimes
import java.util.Calendar

private val WEEKDAY_DUA = listOf("d-sun", "d-mon", "d-tue", "d-wed", "d-thu", "d-fri", "d-sat")

@Composable
fun HomeScreen(
    settings: Settings, pal: DuaaPalette,
    openReader: (String, String) -> Unit, openSheet: (String) -> Unit, goTab: (String) -> Unit
) {
    val now = Calendar.getInstance()
    val t = HijriCalendar.today(settings.hijriAdj.value)
    val loc = settings.location()
    val times = PrayerTimes.compute(now, loc.lat, loc.lng, loc.tz, settings.method.value)
    val nowH = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0
    val (nextKey, dt) = PrayerTimes.next(times, nowH)
    val nextName = PrayerTimes.slots.firstOrNull { it.key == nextKey }?.name ?: ""
    val hh = dt.toInt(); val mm = Math.round((dt - hh) * 60).toInt()
    val wd = now.get(Calendar.DAY_OF_WEEK) - 1
    val occ = Data.occasions.filter { it.m == t.m && it.d == t.d }
    val duaId = WEEKDAY_DUA[wd]
    val dua = Data.categories.first { it.id == "osboo" }.items.first { it.id == duaId }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        ArchCard(pal) {
            Text("بسم الله الرحمن الرحيم", color = pal.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("الدعاء والزيارة", color = pal.emerald, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
            Text("للسيد محمد الحسيني الشيرازي", color = pal.ink2, fontSize = 13.sp)
        }
        PlainCard(pal) {
            Text("${HijriCalendar.weekDays[wd]} ${t.d} ${HijriCalendar.months[t.m - 1]} ${t.y} هـ",
                color = pal.emerald, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Text("${now.get(Calendar.DAY_OF_MONTH)}/${now.get(Calendar.MONTH) + 1}/${now.get(Calendar.YEAR)} م",
                color = pal.ink2, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            occ.forEach { o ->
                Spacer(Modifier.height(8.dp))
                OccBadge(o.k, o.t, pal)
            }
        }
        // الصلاة القادمة
        Row(
            Modifier.fillMaxWidth().padding(bottom = 14.dp).clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.surface).border(1.dp, pal.line, RoundedCornerShape(18.dp))
                .clickable { goTab("prayer") }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("الصلاة القادمة", color = pal.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(nextName, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(PrayerTimes.fmt(PrayerTimes.value(times, nextKey)), color = pal.ink2, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("متبقٍّ", color = pal.ink2, fontSize = 12.sp)
                Text("$hh:${if (mm < 10) "0$mm" else "$mm"}", color = pal.gold, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            }
        }
        SectionTitle("📿 دعاء اليوم — ${HijriCalendar.weekDays[wd]}")
        ItemRow(dua.title, "أدعية أيام الأسبوع", pal) { openReader("osboo", duaId) }
        Spacer(Modifier.height(6.dp))
        SectionTitle("وصول سريع")
        QuickGrid(pal, openReader, openSheet, goTab)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun QuickGrid(pal: DuaaPalette, openReader: (String, String) -> Unit, openSheet: (String) -> Unit, goTab: (String) -> Unit) {
    data class Quick(val ic: String, val label: String, val action: () -> Unit)
    val items = listOf(
        Quick("⭐", "دعاء كميل") { openReader("mashhura", "m-kumayl") },
        Quick("🤲", "التوسّل") { openReader("mashhura", "m-tawassul") },
        Quick("🏴", "عاشوراء") { openReader("ziyarat", "z-ashura") },
        Quick("✦", "الخِيرة") { openSheet("khira") },
        Quick("📿", "التسبيح") { openSheet("tasbih") },
        Quick("🌅", "دعاء العهد") { openReader("mashhura", "m-ahd") },
        Quick("📜", "الجامعة") { openReader("ziyarat", "z-jamia") },
        Quick("📖", "الفهرس") { goTab("lib") }
    )
    Column {
        items.chunked(4).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { q ->
                    Column(
                        Modifier.weight(1f).clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, pal.line, RoundedCornerShape(16.dp))
                            .clickable(onClick = q.action).padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(q.ic, fontSize = 22.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(q.label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface, textAlign = TextAlign.Center)
                    }
                }
                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(vertical = 10.dp))
}

@Composable
fun OccBadge(kind: String, text: String, pal: DuaaPalette) {
    val (bg, fg) = when (kind) {
        "eid" -> pal.good.copy(alpha = .18f) to pal.good
        "birth" -> pal.gold.copy(alpha = .18f) to pal.gold
        "death" -> pal.danger.copy(alpha = .15f) to pal.danger
        else -> pal.surface2 to pal.ink2
    }
    val ic = when (kind) { "death" -> "🏴"; "eid" -> "🌟"; else -> "🌙" }
    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 12.dp, vertical = 5.dp)) {
        Text("$ic $text", color = fg, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ItemRow(title: String, subtitle: String?, pal: DuaaPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 8.dp).clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surface).border(1.dp, pal.line, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            if (subtitle != null) Text(subtitle, color = pal.ink2, fontSize = 12.sp)
        }
        Text("‹", color = pal.ink2, fontSize = 18.sp)
    }
}

// ============ المكتبة + البحث ============
@Composable
fun LibraryScreen(pal: DuaaPalette, openReader: (String, String) -> Unit) {
    var query by remember { mutableStateOf("") }
    var openCat by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        if (openCat == null) {
            // شريط البحث
            Row(
                Modifier.fillMaxWidth().padding(bottom = 14.dp).clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surface).border(1.5.dp, pal.line, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔎", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                TextField(
                    value = query, onValueChange = { query = it },
                    placeholder = { Text("ابحث في الأدعية والزيارات…", color = pal.ink2) },
                    singleLine = true, modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
            if (query.isBlank()) {
                LazyColumn {
                    items(Data.categories) { c ->
                        CatCard(c.icon, c.name, c.desc, c.items.size, pal) { openCat = c.id }
                    }
                }
            } else {
                val hits = ArabicSearch.search(query)
                if (hits.isEmpty()) {
                    PlainCard(pal) { Text("لا توجد نتائج لـ «$query»", color = pal.ink2, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
                } else {
                    Text("${hits.size} نتيجة", color = pal.ink2, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
                    LazyColumn {
                        items(hits) { h ->
                            val sub = h.cat.name + (if (h.snippet.isNotEmpty()) " — ${h.snippet}" else "")
                            ItemRow("${h.cat.icon} ${h.item.title}", sub, pal) { openReader(h.cat.id, h.item.id) }
                        }
                    }
                }
            }
        } else {
            val c = Data.categories.first { it.id == openCat }
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, pal.line, RoundedCornerShape(10.dp)).clickable { openCat = null }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text("›", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(Modifier.width(10.dp))
                Text("${c.icon} ${c.name}", fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = MaterialTheme.colorScheme.onBackground)
            }
            LazyColumn {
                items(c.items) { it2 -> ItemRow(it2.title, it2.src, pal) { openReader(c.id, it2.id) } }
            }
        }
    }
}

@Composable
private fun CatCard(icon: String, name: String, desc: String, count: Int, pal: DuaaPalette, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp).clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface).border(1.dp, pal.line, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(46.dp).clip(RoundedCornerShape(14.dp)).background(pal.emerald.copy(alpha = .12f))
            .border(1.dp, pal.goldSoft, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface)
            Text(desc, color = pal.ink2, fontSize = 12.sp)
        }
        Text("$count", color = pal.gold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
    }
}

// ============ القارئ ============
@Composable
fun ReaderScreen(settings: Settings, pal: DuaaPalette, catId: String, itemId: String, onClose: () -> Unit) {
    val cat = Data.categories.first { it.id == catId }
    val item = cat.items.first { it.id == itemId }
    var tashkeel by remember { mutableStateOf(true) }
    var isFav by remember { mutableStateOf(settings.isFav(catId, itemId)) }
    val clipboard = LocalClipboardManager.current

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            // الشريط العلوي
            Surface(color = pal.emeraldDeep) {
                Row(Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    ReaderBtn("›", pal) { onClose() }
                    Text(item.title, color = Color(0xFFF2EBD6), fontWeight = FontWeight.ExtraBold, fontSize = 15.sp,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp), maxLines = 2)
                    ReaderBtn(if (isFav) "★" else "☆", pal) { settings.toggleFav(catId, itemId); isFav = !isFav }
                    Spacer(Modifier.width(8.dp))
                    ReaderBtn("⧉", pal) { clipboard.setText(AnnotatedString(item.title + "\n\n" + item.text)) }
                }
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(18.dp)) {
                // الأدوات
                Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ToolChip(if (tashkeel) "التشكيل ✓" else "التشكيل", tashkeel, pal) { tashkeel = !tashkeel }
                    ToolChip("حجم −", false, pal) { settings.fontSize.value = (settings.fontSize.value - 2).coerceAtLeast(15f); settings.save() }
                    ToolChip("حجم +", false, pal) { settings.fontSize.value = (settings.fontSize.value + 2).coerceAtMost(40f); settings.save() }
                }
                if (item.src != null) {
                    Box(Modifier.clip(RoundedCornerShape(99.dp)).background(pal.gold.copy(alpha = .14f)).padding(horizontal = 12.dp, vertical = 5.dp)) {
                        Text(item.src, color = pal.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(14.dp))
                }
                if (item.intro != null) {
                    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(pal.surface2).padding(14.dp)) {
                        Text(item.intro, color = pal.ink2, fontSize = 14.sp, lineHeight = 24.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                val text = if (tashkeel) item.text else ArabicSearch.stripTashkeel(item.text)
                SelectionContainer {
                    Text(text, fontSize = settings.fontSize.value.sp, lineHeight = (settings.fontSize.value * 2.0f).sp,
                        color = MaterialTheme.colorScheme.onBackground, textAlign = TextAlign.Justify)
                }
                Spacer(Modifier.height(60.dp))
            }
        }
    }
}

@Composable
private fun ReaderBtn(label: String, pal: DuaaPalette, onClick: () -> Unit) {
    Box(Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(Color.White.copy(alpha = .1f)).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(label, color = Color(0xFFF2EBD6), fontSize = 17.sp)
    }
}

@Composable
private fun ToolChip(label: String, on: Boolean, pal: DuaaPalette, onClick: () -> Unit) {
    Box(
        Modifier.clip(RoundedCornerShape(10.dp))
            .background(if (on) pal.emerald else MaterialTheme.colorScheme.surface)
            .border(1.dp, if (on) pal.emerald else pal.line, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 7.dp)
    ) { Text(label, color = if (on) Color.White else MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}
