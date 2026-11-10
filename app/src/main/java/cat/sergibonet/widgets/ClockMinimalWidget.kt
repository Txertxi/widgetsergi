package cat.sergibonet.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.AlarmClock
import android.widget.RemoteViews
import java.time.ZoneId
import kotlin.math.min

/** Rellotge GMT minimalista (mateixa zona configurable que l'altre rellotge). */
class ClockMinimalWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
        Scheduler.scheduleAt(context, ClockMinimalWidget::class.java, Scheduler.nextFiveMinutes())
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
    ) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        Scheduler.scheduleAt(context, ClockMinimalWidget::class.java, Scheduler.nextFiveMinutes())
    }

    override fun onDisabled(context: Context) {
        Scheduler.cancel(context, ClockMinimalWidget::class.java)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) Prefs.clear(context, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Scheduler.ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, ClockMinimalWidget::class.java))
                if (ids.isNotEmpty()) {
                    for (id in ids) update(context, mgr, id)
                    Scheduler.scheduleAt(context, ClockMinimalWidget::class.java, Scheduler.nextFiveMinutes())
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, ClockMinimalWidget::class.java))) update(context, mgr, id)
        }

        fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val zoneId = Prefs.getZone(context, id)
            val zone = try {
                ZoneId.of(zoneId)
            } catch (e: Exception) {
                ZoneId.of("UTC")
            }
            val (w, h) = WidgetUtil.sizePx(context, mgr.getAppWidgetOptions(id), 150, 150, 700_000)
            val bmp = ClockMinimalRenderer.render(min(w, h), zone)

            val rv = RemoteViews(context.packageName, R.layout.widget_clock_min)
            rv.setImageViewBitmap(R.id.clock_dial, bmp)
            val alarms = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            rv.setOnClickPendingIntent(
                R.id.clock_root,
                PendingIntent.getActivity(context, 0, alarms, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            )
            mgr.updateAppWidget(id, rv)
        }
    }
}
