package com.shirazi.duaa.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shirazi.duaa.data.*
import com.shirazi.duaa.logic.ArabicSearch
import com.shirazi.duaa.logic.HijriCalendar
import com.shirazi.duaa.logic.PrayerTimes
import com.shirazi.duaa.notify.PrayerScheduler
import java.util.Calendar

// مفاتيح الوصول السريع
private data class Tab(val key: String, val label: String, val icon: String)
private val TABS = listOf(
    Tab("home", "الرئيسية", "🕌"), Tab("lib", "الفهرس", "📖"),
    Tab("prayer", "الأوقات", "🕰️"), Tab("cal", "التقويم", "🌙"), Tab("more", "المزيد", "📿")
)

@Composable
fun DuaaApp(settings: Settings, onRequestNotifyPermission: () -> Unit) {
    val dark = settings.theme.value == "dark"
    DuaaTheme(dark) {
        val pal = palette(dark)
        var tab by remember { mutableStateOf("home") }
        var reader by remember { mutableStateOf<Pair<String, String>?>(null) }
        var sheet by remember { mutableStateOf<String?>(null) } // khira/tasbih/fav/settings/about/loc/method

      CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                Header(pal,
                    onTheme = { settings.theme.value = if (dark) "light" else "dark"; settings.save() },
                    onSettings = { sheet = "settings" })
            },
            bottomBar = {
                NavBar(tab, pal) { tab = it }
            }
        ) { pad ->
            Box(Modifier.padding(pad).fillMaxSize()) {
                when (tab) {
                    "home" -> HomeScreen(settings, pal, openReader = { c, i -> reader = c to i }, openSheet = { sheet = it }, goTab = { tab = it })
                    "lib" -> LibraryScreen(pal) { c, i -> reader = c to i }
                    "prayer" -> PrayerScreen(settings, pal, openSheet = { sheet = it })
                    "cal" -> CalendarScreen(settings, pal)
                    "more" -> MoreScreen(pal) { sheet = it }
                }
            }
        }

        // القارئ
        reader?.let { (c, i) ->
            ReaderScreen(settings, pal, c, i, onClose = { reader = null })
        }

        // الأوراق السفلية
        when (sheet) {
            "khira" -> KhiraSheet(pal) { sheet = null }
            "tasbih" -> TasbihSheet(pal) { sheet = null }
            "fav" -> FavSheet(settings, pal, onClose = { sheet = null }, openReader = { c, i -> sheet = null; reader = c to i })
            "settings" -> SettingsSheet(settings, pal, onClose = { sheet = null }, openSheet = { sheet = it }, onRequestNotifyPermission = onRequestNotifyPermission)
            "about" -> AboutSheet(pal) { sheet = null }
            "loc" -> LocationSheet(settings, pal) { sheet = null }
            "method" -> MethodSheet(settings, pal) { sheet = null }
        }
      }
    }
}

@Composable
private fun Header(pal: DuaaPalette, onTheme: () -> Unit, onSettings: () -> Unit) {
    Surface(color = pal.emeraldDeep) {
        Row(
            Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("الدعاء والزيارة", color = Color(0xFFF2EBD6), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                Text("السيد محمد الحسيني الشيرازي — يعمل دون إنترنت", color = Color(0xFFF2EBD6).copy(alpha = .8f), fontSize = 11.sp)
            }
            HeaderBtn("◐", pal, onTheme)
            Spacer(Modifier.width(8.dp))
            HeaderBtn("⚙", pal, onSettings)
        }
    }
}

@Composable
private fun HeaderBtn(label: String, pal: DuaaPalette, onClick: () -> Unit) {
    Box(
        Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = .10f))
            .border(1.dp, pal.goldSoft.copy(alpha = .35f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Text(label, color = Color(0xFFF2EBD6), fontSize = 18.sp) }
}

@Composable
private fun NavBar(current: String, pal: DuaaPalette, onSelect: (String) -> Unit) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
        TABS.forEach { t ->
            NavigationBarItem(
                selected = current == t.key,
                onClick = { onSelect(t.key) },
                icon = { Text(t.icon, fontSize = 20.sp) },
                label = { Text(t.label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = pal.emerald, selectedTextColor = pal.emerald,
                    indicatorColor = pal.emerald.copy(alpha = .12f),
                    unselectedIconColor = pal.ink2, unselectedTextColor = pal.ink2
                )
            )
        }
    }
}

// ============ بطاقات مساعدة ============
@Composable
fun ArchCard(pal: DuaaPalette, content: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier.fillMaxWidth().padding(bottom = 14.dp)
            .clip(RoundedCornerShape(topStart = 110.dp, topEnd = 110.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.5.dp, pal.goldSoft, RoundedCornerShape(topStart = 110.dp, topEnd = 110.dp, bottomStart = 18.dp, bottomEnd = 18.dp))
            .padding(top = 26.dp, bottom = 18.dp, start = 18.dp, end = 18.dp)
    ) { Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, content = content) }
}

@Composable
fun PlainCard(pal: DuaaPalette, content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxWidth().padding(bottom = 14.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, pal.line, RoundedCornerShape(18.dp))
            .padding(16.dp),
        content = content
    )
}
