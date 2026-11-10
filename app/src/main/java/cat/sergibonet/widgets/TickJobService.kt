package cat.sergibonet.widgets

import android.app.job.JobParameters
import android.app.job.JobService

/** Refresca tots els widgets (via de seguretat a part de les alarmes). */
class TickJobService : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        ClockWidget.updateAll(this)
        ClockMinimalWidget.updateAll(this)
        CalendarWidget.updateAll(this)
        return false
    }

    override fun onStopJob(params: JobParameters?): Boolean = true
}
