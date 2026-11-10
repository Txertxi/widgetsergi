package cat.sergibonet.widgets

import android.content.Context
import org.json.JSONArray
import java.time.LocalDate

/**
 * STATE = estatal, CATALONIA = Catalunya, CAPITAL = Barcelona (capital),
 * LOCAL = festa local. Cada versió té el seu holidays.json (app/src/main/assets
 * per a Sergi, app/src/lluis/assets per al Lluís).
 */
enum class Level { STATE, CATALONIA, CAPITAL, LOCAL }

data class Holiday(
    val date: LocalDate,
    val name: String,
    val level: Level,
    val place: String?,
    val provisional: Boolean
) {
    fun label(): String =
        name + (if (place != null) " ($place)" else "") + (if (provisional) "*" else "")
}

/** Llegeix assets/holidays.json (editable a mà: afegeix-hi els festius de cada any). */
object Holidays {
    private var cache: Map<LocalDate, List<Holiday>>? = null

    @Synchronized
    fun load(context: Context): Map<LocalDate, List<Holiday>> {
        cache?.let { return it }
        val text = context.assets.open("holidays.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val arr = JSONArray(text)
        val list = ArrayList<Holiday>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val level = when (o.getString("level")) {
                "state" -> Level.STATE
                "catalonia" -> Level.CATALONIA
                "capital" -> Level.CAPITAL
                else -> Level.LOCAL
            }
            val place = o.optString("place", "").takeIf { it.isNotEmpty() }
            list.add(
                Holiday(
                    LocalDate.parse(o.getString("date")),
                    o.getString("name"),
                    level,
                    place,
                    o.optBoolean("provisional", false)
                )
            )
        }
        val map = list.groupBy { it.date }
        cache = map
        return map
    }

    /** Si un dia en té diversos, mana l'estatal, després el català, la capital i al final el local. */
    fun strongest(list: List<Holiday>?): Holiday? = list?.minByOrNull { it.level.ordinal }

    /** Els primers [limit] dies festius entre [from] i [to] (tots dos inclosos), un festiu per dia. */
    fun upcoming(map: Map<LocalDate, List<Holiday>>, from: LocalDate, to: LocalDate, limit: Int): List<Holiday> =
        map.keys.filter { !it.isBefore(from) && !it.isAfter(to) }
            .sorted()
            .take(limit)
            .mapNotNull { strongest(map[it]) }

    /** Primer festiu a partir de [from] (inclòs). */
    fun next(map: Map<LocalDate, List<Holiday>>, from: LocalDate): Holiday? {
        val d = map.keys.filter { !it.isBefore(from) }.minOrNull() ?: return null
        return strongest(map[d])
    }
}
