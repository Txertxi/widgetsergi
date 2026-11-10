package cat.sergibonet.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import java.time.Instant
import java.time.ZoneId
import java.util.TimeZone

/** Tria la zona horària de l'agulla GMT (s'obre en afegir el widget i en "Reconfigura"). */
class ClockConfigActivity : Activity() {
    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var shown: List<String> = emptyList()
    private lateinit var adapter: ArrayAdapter<String>

    private val allIds: List<String> by lazy {
        TimeZone.getAvailableIDs().filter {
            (it.contains('/') && !it.startsWith("Etc/") && !it.startsWith("SystemV/")) || it == "UTC" || it == "GMT"
        }.sorted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)
        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val pad = (16 * resources.displayMetrics.density).toInt()
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(pad, pad, pad, pad)

        val title = TextView(this)
        title.text = "Segona zona horària (agulla GMT de 24 h)"
        title.textSize = 18f

        val search = EditText(this)
        search.hint = "Cerca: London, New York, Tokyo, UTC…"
        search.isSingleLine = true

        val list = ListView(this)
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, ArrayList<String>())
        list.adapter = adapter

        root.addView(title)
        root.addView(search)
        root.addView(list, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f))
        setContentView(root)

        refresh("")
        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                refresh(s?.toString() ?: "")
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
        list.setOnItemClickListener { _, _, position, _ ->
            if (position in shown.indices) choose(shown[position])
        }
    }

    private fun refresh(q: String) {
        val query = q.trim().replace(' ', '_').lowercase()
        shown = allIds.filter { query.isEmpty() || it.lowercase().contains(query) }
        val now = Instant.now()
        val items = shown.map { id ->
            val off = try {
                val o = ZoneId.of(id).rules.getOffset(now)
                if (o.totalSeconds == 0) "UTC" else "UTC" + o.id
            } catch (e: Exception) {
                ""
            }
            id.replace('_', ' ') + "   (" + off + ")"
        }
        adapter.clear()
        adapter.addAll(items)
        adapter.notifyDataSetChanged()
    }

    private fun choose(zoneId: String) {
        Prefs.setZone(this, widgetId, zoneId)
        val mgr = AppWidgetManager.getInstance(this)
        val provider = mgr.getAppWidgetInfo(widgetId)?.provider?.className
        if (provider == ClockMinimalWidget::class.java.name) {
            ClockMinimalWidget.update(this, mgr, widgetId)
        } else {
            ClockWidget.update(this, mgr, widgetId)
        }
        setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
        finish()
    }
}
