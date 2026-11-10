package cat.sergibonet.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.RemoteViews
import java.time.LocalDate

class CalendarWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) update(context, appWidgetManager, id)
        Scheduler.scheduleAt(context, CalendarWidget::class.java, Scheduler.nextMidnight())
    }

    override fun onAppWidgetOptionsChanged(
        context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, newOptions: Bundle
    ) {
        update(context, appWidgetManager, appWidgetId)
    }

    override fun onEnabled(context: Context) {
        Scheduler.scheduleAt(context, CalendarWidget::class.java, Scheduler.nextMidnight())
    }

    override fun onDisabled(context: Context) {
        Scheduler.cancel(context, CalendarWidget::class.java)
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (id in appWidgetIds) Prefs.clear(context, id)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_PREV, ACTION_NEXT, ACTION_RESET -> {
                val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val today = LocalDate.now()
                    val cur = Prefs.getOffset(context, id, today)
                    val next = when (intent.action) {
                        ACTION_PREV -> cur - 1
                        ACTION_NEXT -> cur + 1
                        else -> 0
                    }
                    Prefs.setOffset(context, id, next, today)
                    update(context, AppWidgetManager.getInstance(context), id)
                }
            }
            Scheduler.ACTION_TICK,
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                val mgr = AppWidgetManager.getInstance(context)
                val ids = mgr.getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))
                if (ids.isNotEmpty()) {
                    for (id in ids) update(context, mgr, id)
                    Scheduler.scheduleAt(context, CalendarWidget::class.java, Scheduler.nextMidnight())
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        fun updateAll(context: Context) {
            val mgr = AppWidgetManager.getInstance(context)
            for (id in mgr.getAppWidgetIds(ComponentName(context, CalendarWidget::class.java))) update(context, mgr, id)
        }

        const val ACTION_PREV = "cat.sergibonet.widgets.CAL_PREV"
        const val ACTION_NEXT = "cat.sergibonet.widgets.CAL_NEXT"
        const val ACTION_RESET = "cat.sergibonet.widgets.CAL_RESET"

        fun update(context: Context, mgr: AppWidgetManager, id: Int) {
            val today = LocalDate.now()
            val offset = Prefs.getOffset(context, id, today)
            val (w, h) = WidgetUtil.sizePx(context, mgr.getAppWidgetOptions(id), 300, 150, 1_300_000)
            val bmp = CalendarRenderer.render(context, w, h, offset, today)

            val rv = RemoteViews(context.packageName, R.layout.widget_calendar)
            rv.setImageViewBitmap(R.id.cal_image, bmp)
            rv.setOnClickPendingIntent(R.id.zone_prev, broadcast(context, id, ACTION_PREV, 1))
            rv.setOnClickPendingIntent(R.id.zone_next, broadcast(context, id, ACTION_NEXT, 2))
            rv.setOnClickPendingIntent(R.id.zone_title, broadcast(context, id, ACTION_RESET, 3))
            val open = openCalendar(context)
            rv.setOnClickPendingIntent(R.id.zone_left, open)
            rv.setOnClickPendingIntent(R.id.zone_body, open)
            mgr.updateAppWidget(id, rv)
        }

        private fun broadcast(context: Context, id: Int, action: String, n: Int): PendingIntent {
            val i = Intent(context, CalendarWidget::class.java)
                .setAction(action)
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
            return PendingIntent.getBroadcast(
                context, id * 10 + n, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun openCalendar(context: Context): PendingIntent {
            val i = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_CALENDAR)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context, 0, i, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
