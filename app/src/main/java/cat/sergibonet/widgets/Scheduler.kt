package cat.sergibonet.widgets

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

/**
 * Refresc dels widgets amb DUES vies independents (redundància):
 * 1) alarma inexacta cada 5 min (rellotges) o a mitjanit (calendari);
 * 2) tasca periòdica del sistema (JobScheduler) cada 15 min, que refresca tots els widgets.
 */
object Scheduler {
    const val ACTION_TICK = "cat.sergibonet.widgets.TICK"
    private const val JOB_ID = 4201

    private fun pending(ctx: Context, cls: Class<*>): PendingIntent {
        val i = Intent(ctx, cls).setAction(ACTION_TICK)
        return PendingIntent.getBroadcast(
            ctx, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleAt(ctx: Context, cls: Class<*>, atMillis: Long) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.setAndAllowWhileIdle(AlarmManager.RTC, atMillis, pending(ctx, cls))
        ensureJob(ctx)
    }

    fun cancel(ctx: Context, cls: Class<*>) {
        val am = ctx.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pending(ctx, cls))
    }

    /** Tasca periòdica de seguretat: si les alarmes fallen, aquesta continua refrescant. */
    fun ensureJob(ctx: Context) {
        try {
            val js = ctx.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            if (js.getPendingJob(JOB_ID) != null) return
            val info = JobInfo.Builder(JOB_ID, ComponentName(ctx, TickJobService::class.java))
                .setPeriodic(15 * 60 * 1000L)
                .setPersisted(true)
                .build()
            js.schedule(info)
        } catch (e: Exception) {
            // si el sistema no ho permet, ens quedem només amb les alarmes
        }
    }

    fun nextMidnight(): Long =
        LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() + 2000L

    /** Cada 5 minuts: l'agulla GMT es mou 1,25° en 5 min (imperceptible). */
    fun nextFiveMinutes(): Long {
        val t = System.currentTimeMillis()
        return (t / 300_000L + 1) * 300_000L + 500L
    }
}
