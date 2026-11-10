package cat.sergibonet.widgets

import android.content.Context
import java.time.LocalDate

object Prefs {
    private fun sp(ctx: Context) = ctx.getSharedPreferences("widgets", Context.MODE_PRIVATE)

    // --- Rellotge: segona zona horària per widget
    fun getZone(ctx: Context, id: Int): String = sp(ctx).getString("zone_$id", "UTC") ?: "UTC"

    fun setZone(ctx: Context, id: Int, zone: String) {
        sp(ctx).edit().putString("zone_$id", zone).apply()
    }

    // --- Calendari: desplaçament de mes (només val el dia en què s'ha triat; l'endemà torna al mes actual)
    fun getOffset(ctx: Context, id: Int, today: LocalDate): Int {
        val p = sp(ctx)
        return if (p.getString("offdate_$id", null) == today.toString()) p.getInt("off_$id", 0) else 0
    }

    fun setOffset(ctx: Context, id: Int, offset: Int, today: LocalDate) {
        sp(ctx).edit().putInt("off_$id", offset).putString("offdate_$id", today.toString()).apply()
    }

    fun clear(ctx: Context, id: Int) {
        sp(ctx).edit().remove("zone_$id").remove("off_$id").remove("offdate_$id").apply()
    }
}
