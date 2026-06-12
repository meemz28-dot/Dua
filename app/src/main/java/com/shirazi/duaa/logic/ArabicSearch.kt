package com.shirazi.duaa.logic

import com.shirazi.duaa.data.Category
import com.shirazi.duaa.data.Data
import com.shirazi.duaa.data.DuaaItem

/** تطبيع النص العربي للبحث: حذف التشكيل وتوحيد الهمزات والحروف. */
object ArabicSearch {

    private val tashkeel = Regex("[\u064B-\u0652\u0670\u0640\u0653-\u065F]")

    fun stripTashkeel(s: String): String = tashkeel.replace(s, "")

    fun normalize(s: String): String =
        stripTashkeel(s)
            .replace(Regex("[أإآ]"), "ا")
            .replace("ى", "ي")
            .replace("ة", "ه")
            .replace("ؤ", "و")
            .replace("ئ", "ي")
            .replace(Regex("\\s+"), " ")
            .trim()

    data class Hit(val cat: Category, val item: DuaaItem, val snippet: String)

    fun search(query: String): List<Hit> {
        val nq = normalize(query)
        if (nq.isEmpty()) return emptyList()
        val hits = ArrayList<Hit>()
        for (c in Data.categories) for (it in c.items) {
            val inTitle = normalize(it.title).contains(nq)
            val flatText = it.text.replace("\n", " ")
            val normText = normalize(flatText)
            val inBody = normText.contains(nq)
            if (inTitle || inBody) {
                var snippet = ""
                if (inBody) {
                    val plain = stripTashkeel(flatText)
                    val ix = normText.indexOf(nq)
                    if (ix >= 0) {
                        val st = (ix - 20).coerceAtLeast(0)
                        val end = (st + 75).coerceAtMost(plain.length)
                        snippet = (if (st > 0) "…" else "") + plain.substring(st, end) + "…"
                    }
                }
                hits.add(Hit(c, it, snippet))
            }
        }
        return hits
    }
}
