package cat.sergibonet.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

/** Alarmes inexactes (no cal cap permís) per refrescar els widgets. */
object Scheduler {
    const val ACTION_TICK = "cat.sergibonet.widgets.TICK"

    private fun pending(ctx: Context, cls: Class<*>): PendingIntent {
        val i = Intent(ctx, cls).setAction(ACTION_TICK)
        return PendingIntent.getBroadcast(
            ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleAt(ctx: Context, cls: Class<*>, atMillis: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC, atMillis, pending(ctx, cls))
    }

    fun cancel(ctx: Context, cls: Class<*>) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(ctx, cls))
    }

    fun nextMidnight(): Long =
        LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 2000L

    /** Cada 5 minuts: l'agulla GMT es mou 1,25° en 5 min (imperceptible). */
    fun nextFiveMinutes(): Long {
        val t = System.currentTimeMillis()
        return (t / 300_000L + 1) * 300_000L + 500L
    }
}
