package com.shirazi.duaa.data

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf

/** حفظ الإعدادات والمفضلة محلياً على الجهاز. */
class Settings(context: Context) {
    private val sp = context.getSharedPreferences("duaa_prefs", Context.MODE_PRIVATE)

    var theme = mutableStateOf(sp.getString("theme", "light") ?: "light")
    var fontSize = mutableStateOf(sp.getFloat("fontSize", 21f))
    var hijriAdj = mutableStateOf(sp.getInt("hijriAdj", 0))
    var method = mutableStateOf(sp.getString("method", "jafari") ?: "jafari")
    var cityIdx = mutableStateOf(sp.getInt("cityIdx", 0))
    var customLat = mutableStateOf<Double?>(if (sp.contains("lat")) sp.getFloat("lat", 0f).toDouble() else null)
    var customLng = mutableStateOf<Double?>(if (sp.contains("lng")) sp.getFloat("lng", 0f).toDouble() else null)
    var customTz = mutableStateOf(sp.getFloat("tz", 0f).toDouble())
    var customName = mutableStateOf<String?>(sp.getString("cityName", null))
    var notifyEnabled = mutableStateOf(sp.getBoolean("notify", false))

    // favorites stored as "catId|itemId" lines
    val favorites = mutableStateListOf<Pair<String, String>>().also { list ->
        sp.getString("fav", "")?.split("\n")?.forEach { line ->
            val p = line.split("|")
            if (p.size == 2 && p[0].isNotEmpty()) list.add(p[0] to p[1])
        }
    }

    fun save() {
        sp.edit().apply {
            putString("theme", theme.value)
            putFloat("fontSize", fontSize.value)
            putInt("hijriAdj", hijriAdj.value)
            putString("method", method.value)
            putInt("cityIdx", cityIdx.value)
            val lat = customLat.value; val lng = customLng.value
            if (lat != null && lng != null) {
                putFloat("lat", lat.toFloat()); putFloat("lng", lng.toFloat())
            } else { remove("lat"); remove("lng") }
            putFloat("tz", customTz.value.toFloat())
            putString("cityName", customName.value)
            putBoolean("notify", notifyEnabled.value)
            apply()
        }
    }

    fun saveFav() {
        sp.edit().putString("fav", favorites.joinToString("\n") { "${it.first}|${it.second}" }).apply()
    }

    fun isFav(c: String, i: String) = favorites.any { it.first == c && it.second == i }
    fun toggleFav(c: String, i: String) {
        val idx = favorites.indexOfFirst { it.first == c && it.second == i }
        if (idx >= 0) favorites.removeAt(idx) else favorites.add(c to i)
        saveFav()
    }

    /** الموقع الفعّال: مخصّص أو مدينة مختارة. */
    fun location(): City {
        val lat = customLat.value; val lng = customLng.value
        return if (lat != null && lng != null)
            City(customName.value ?: "موقع مخصّص", lat, lng, customTz.value)
        else Data.cities[cityIdx.value.coerceIn(0, Data.cities.size - 1)]
    }
}
