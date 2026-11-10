package cat.sergibonet.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import kotlin.math.max
import kotlin.math.sqrt

object WidgetUtil {
    /** Mida real del widget en píxels (segons orientació), limitada per no passar-se de memòria. */
    fun sizePx(context: Context, options: Bundle?, defWdp: Int, defHdp: Int, maxPixels: Int): Pair<Int, Int> {
        val dm = context.resources.displayMetrics
        val land = context.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        var wdp = 0
        var hdp = 0
        if (options != null) {
            wdp = options.getInt(
                if (land) AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH else AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 0
            )
            hdp = options.getInt(
                if (land) AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT else AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, 0
            )
        }
        if (wdp <= 0) wdp = defWdp
        if (hdp <= 0) hdp = defHdp
        var w = (wdp * dm.density).toInt()
        var h = (hdp * dm.density).toInt()
        val px = w.toLong() * h.toLong()
        if (px > maxPixels) {
            val f = sqrt(maxPixels.toDouble() / px.toDouble())
            w = (w * f).toInt()
            h = (h * f).toInt()
        }
        return Pair(max(w, 100), max(h, 100))
    }
}
