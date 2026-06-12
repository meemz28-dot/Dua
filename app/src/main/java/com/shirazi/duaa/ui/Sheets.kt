package com.shirazi.duaa.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.shirazi.duaa.data.Data
import com.shirazi.duaa.data.Settings
import com.shirazi.duaa.logic.PrayerTimes
import com.shirazi.duaa.notify.PrayerScheduler
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Sheet(title: String, onClose: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(onDismissRequest = onClose, containerColor = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 30.dp).verticalScroll(rememberScrollState())) {
            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(bottom = 14.dp))
            content()
        }
    }
}

// ============ الخيرة ============
@Composable
fun KhiraSheet(pal: DuaaPalette, onClose: () -> Unit) {
    var result by remember { mutableStateOf<com.shirazi.duaa.data.KhiraAyah?>(null) }
    Sheet("خِيرة القرآن الكريم ✦", onClose) {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(pal.surface2).padding(14.dp)) {
            Text(Data.khiraAdab, color = pal.ink2, fontSize = 14.sp, lineHeight = 24.sp)
        }
        Spacer(Modifier.height(14.dp))
        BigButton("✦ استخر الآن", pal, true) { result = Data.khiraAyat.random() }
        result?.let { a ->
            Spacer(Modifier.height(14.dp))
            PlainCard(pal) {
                Text("﴿ ${a.t} ﴾", fontSize = 22.sp, lineHeight = 40.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(8.dp))
                Box(Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(99.dp)).background(pal.gold.copy(alpha = .14f)).padding(horizontal = 12.dp, vertical = 4.dp)) {
                    Text(a.s, color = pal.gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(14.dp))
                val (bg, fg) = when (a.v) { "good" -> pal.good.copy(alpha = .16f) to pal.good; "bad" -> pal.danger.copy(alpha = .14f) to pal.danger; else -> pal.mid.copy(alpha = .16f) to pal.mid }
                Box(Modifier.align(Alignment.CenterHorizontally).clip(RoundedCornerShape(99.dp)).background(bg).padding(horizontal = 18.dp, vertical = 6.dp)) {
                    Text(a.m, color = fg, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

// ============ التسبيح ============
@Composable
fun TasbihSheet(pal: DuaaPalette, onClose: () -> Unit) {
    val stages = listOf("الله أكبر" to 34, "الحمد لله" to 33, "سبحان الله" to 33)
    var count by remember { mutableStateOf(0) }
    var stage by remember { mutableStateOf(0) } // -1 = free, >=size = done
    Sheet("تسبيح الزهراء (ع) 📿", onClose) {
        val free = stage < 0; val done = stage >= stages.size
        val target = if (free || done) 33 else stages[stage].second
        val frac = if (free) (count % 33) / 33f else if (done) 1f else min(1f, count.toFloat() / target)
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(230.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val sw = 20f; val d = min(size.width, size.height) - sw
                    val topLeft = Offset((size.width - d) / 2, (size.height - d) / 2)
                    drawArc(pal.line, -90f, 360f, false, topLeft = topLeft, size = Size(d, d), style = Stroke(sw, cap = StrokeCap.Round))
                    drawArc(pal.gold, -90f, 360f * frac, false, topLeft = topLeft, size = Size(d, d), style = Stroke(sw, cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$count", fontSize = 48.sp, fontWeight = FontWeight.ExtraBold, color = pal.emerald)
                    Text(if (free) "عدّاد حر" else if (done) "✓ تمّ" else stages[stage].first, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    if (!free && !done) Text("$count / ${stages[stage].second}", color = pal.ink2, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(
                Modifier.size(120.dp).clip(CircleShape).background(pal.emerald)
                    .clickable {
                        count++
                        if (stage in stages.indices && count >= stages[stage].second) { stage++; count = 0 }
                    }, contentAlignment = Alignment.Center
            ) { Text("سبِّح", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp) }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) { BigButton("↺ إعادة", pal, true) { count = 0; stage = 0 } }
                Box(Modifier.weight(1f)) { BigButton("عدّاد حر", pal, true) { stage = -1; count = 0 } }
            }
        }
    }
}

// ============ المفضلة ============
@Composable
fun FavSheet(settings: Settings, pal: DuaaPalette, onClose: () -> Unit, openReader: (String, String) -> Unit) {
    Sheet("المفضلة ★", onClose) {
        if (settings.favorites.isEmpty()) {
            Text("لم تُضِف أي دعاء بعد.\nافتح أي دعاء واضغط ☆ لحفظه هنا.", color = pal.ink2, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(20.dp), lineHeight = 24.sp)
        } else {
            settings.favorites.toList().forEach { (cId, iId) ->
                val cat = Data.categories.firstOrNull { it.id == cId }
                val item = cat?.items?.firstOrNull { it.id == iId }
                if (cat != null && item != null) ItemRow("${cat.icon} ${item.title}", cat.name, pal) { openReader(cId, iId) }
            }
        }
    }
}

// ============ الإعدادات ============
@Composable
fun SettingsSheet(settings: Settings, pal: DuaaPalette, onClose: () -> Unit, openSheet: (String) -> Unit, onRequestNotifyPermission: () -> Unit) {
    val ctx = LocalContext.current
    Sheet("الإعدادات ⚙️", onClose) {
        Text("المظهر", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Segmented(listOf("فاتح" to "light", "داكن" to "dark"), settings.theme.value, pal) { settings.theme.value = it; settings.save() }
        Spacer(Modifier.height(14.dp))

        Text("حجم خط الأدعية: ${settings.fontSize.value.toInt()}", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { BigButton("A −", pal, true) { settings.fontSize.value = (settings.fontSize.value - 1).coerceAtLeast(15f); settings.save() } }
            Box(Modifier.weight(1f)) { BigButton("A +", pal, true) { settings.fontSize.value = (settings.fontSize.value + 1).coerceAtMost(40f); settings.save() } }
        }
        Spacer(Modifier.height(14.dp))

        Text("تنبيهات أوقات الصلاة", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).border(1.dp, pal.line, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(if (settings.notifyEnabled.value) "مفعّلة" else "متوقّفة", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Switch(checked = settings.notifyEnabled.value, onCheckedChange = { on ->
                settings.notifyEnabled.value = on; settings.save()
                if (on) { onRequestNotifyPermission(); PrayerScheduler.reschedule(ctx) } else PrayerScheduler.cancelAll(ctx)
            }, colors = SwitchDefaults.colors(checkedTrackColor = pal.emerald))
        }
        Spacer(Modifier.height(14.dp))

        Text("طريقة حساب المواقيت", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        BigButton(PrayerTimes.methodByKey(settings.method.value).name, pal, true) { openSheet("method") }
        Spacer(Modifier.height(14.dp))

        Text("الموقع", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        BigButton(settings.location().name, pal, true) { openSheet("loc") }
        Spacer(Modifier.height(14.dp))

        Text("تعديل التقويم الهجري (${if (settings.hijriAdj.value > 0) "+" else ""}${settings.hijriAdj.value} يوم)", color = pal.ink2, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.weight(1f)) { BigButton("− يوم", pal, true) { settings.hijriAdj.value = (settings.hijriAdj.value - 1).coerceAtLeast(-2); settings.save() } }
            Box(Modifier.weight(1f)) { BigButton("+ يوم", pal, true) { settings.hijriAdj.value = (settings.hijriAdj.value + 1).coerceAtMost(2); settings.save() } }
        }
        Spacer(Modifier.height(10.dp))
        Text("عدّل التقويم ±يوم أو يومين عند اختلاف رؤية الهلال محلياً.", color = pal.ink2, fontSize = 12.sp)
    }
}

@Composable
private fun Segmented(options: List<Pair<String, String>>, selected: String, pal: DuaaPalette, onSelect: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(pal.surface2).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        options.forEach { (label, value) ->
            val on = value == selected
            Box(Modifier.weight(1f).clip(RoundedCornerShape(9.dp)).background(if (on) MaterialTheme.colorScheme.surface else Color.Transparent).clickable { onSelect(value) }.padding(vertical = 9.dp), contentAlignment = Alignment.Center) {
                Text(label, color = if (on) pal.emerald else pal.ink2, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// ============ حول ============
@Composable
fun AboutSheet(pal: DuaaPalette, onClose: () -> Unit) {
    Sheet("حول الكتاب والتطبيق ℹ️", onClose) {
        val p = MaterialTheme.colorScheme.onBackground
        Text("كتاب الدعاء والزيارة", fontWeight = FontWeight.ExtraBold, color = p, fontSize = 15.sp)
        Text("للمرجع الديني الراحل السيد محمد الحسيني الشيرازي (قدّس سرّه).", color = p, lineHeight = 26.sp)
        Spacer(Modifier.height(10.dp))
        Text("يجمع التطبيق مختارات من أدعية الكتاب وزياراته مرتّبةً على أبوابه: التعقيبات، أدعية الصباح والمساء، أدعية الأسبوع، أعمال الجمعة، الأدعية المشهورة، المناجاة، أدعية الحوائج، أعمال رمضان، والزيارات.", color = p, lineHeight = 26.sp)
        Spacer(Modifier.height(10.dp))
        Text("الميزات: بحث في كامل النصوص، خِيرة القرآن، مواقيت الصلاة المحسوبة فلكياً مع تنبيهاتها، تقويم هجري بمناسبات الشيعة الإمامية، تسبيح الزهراء، والمفضلة.", color = p, lineHeight = 26.sp)
        Spacer(Modifier.height(12.dp))
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(pal.surface2).padding(12.dp)) {
            Text("⚠️ النصوص بنُسخها المتداولة المعروفة وقد تختلف يسيراً عن الطبعة الورقية؛ يُرجى مراجعة المطبوع عند الحاجة للدقّة، خصوصاً في الزيارات الطويلة. المواقيت وبدايات الأشهر تقريبية فاحتَط في العبادات.", color = pal.ink2, fontSize = 13.sp, lineHeight = 22.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("نسخة 1.0 — تعمل دون اتصال — بياناتك محفوظة على جهازك فقط.", color = pal.ink2, fontSize = 12.sp)
    }
}

// ============ الموقع ============
@Composable
fun LocationSheet(settings: Settings, pal: DuaaPalette, onClose: () -> Unit) {
    val ctx = LocalContext.current
    Sheet("اختر الموقع 📍", onClose) {
        Data.cities.forEachIndexed { i, c ->
            val on = settings.customLat.value == null && settings.cityIdx.value == i
            Row(Modifier.fillMaxWidth().clickable {
                settings.cityIdx.value = i; settings.customLat.value = null; settings.customLng.value = null
                settings.customTz.value = c.tz; settings.customName.value = null; settings.save()
                PrayerScheduler.reschedule(ctx); onClose()
            }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (on) "✓ ${c.n}" else c.n, fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Normal, color = if (on) pal.emerald else MaterialTheme.colorScheme.onSurface)
            }
            Divider(color = pal.line)
        }
    }
}

// ============ طريقة الحساب ============
@Composable
fun MethodSheet(settings: Settings, pal: DuaaPalette, onClose: () -> Unit) {
    val ctx = LocalContext.current
    Sheet("طريقة حساب المواقيت", onClose) {
        PrayerTimes.methods.forEach { m ->
            val on = m.key == settings.method.value
            Row(Modifier.fillMaxWidth().clickable {
                settings.method.value = m.key; settings.save(); PrayerScheduler.reschedule(ctx); onClose()
            }.padding(vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(if (on) "✓ ${m.name}" else m.name, fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Normal, color = if (on) pal.emerald else MaterialTheme.colorScheme.onSurface)
            }
            Divider(color = pal.line)
        }
    }
}
