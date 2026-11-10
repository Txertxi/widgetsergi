package cat.sergibonet.widgets

import android.app.Activity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (20 * resources.displayMetrics.density).toInt()
        val tv = TextView(this)
        tv.setPadding(pad, pad, pad, pad)
        tv.textSize = 16f
        tv.text = "Widgets Sergi\n\n" +
            "Aquesta app no té pantalla: només aporta dos widgets.\n\n" +
            "Per afegir-los: mantén premut un espai buit de la pantalla d'inici → Widgets → " +
            "\"Widgets Sergi\" → Calendari (català) i Rellotge GMT.\n\n" +
            "Rellotge: en afegir-lo triaràs la segona zona horària. Per canviar-la després, " +
            "mantén premut el widget i toca \"Reconfigura\" (segons el llançador).\n\n" +
            "Calendari: toca ‹ o › per canviar de mes, el títol per tornar al mes actual " +
            "i la resta per obrir el calendari.\n\n" +
            getString(R.string.holiday_legend)
        val sv = ScrollView(this)
        sv.addView(tv)
        setContentView(sv)
    }
}
