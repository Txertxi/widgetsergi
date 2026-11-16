package cat.sergibonet.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dibuixa la capa de fons del rellotge GMT (tot en coordenades 0..400):
 * números de 24 h amb un cercle fi nit/dia per fora, índexs, finestreta de data,
 * marca i agulla GMT. Les agulles d'hores, minuts i segons les posa AnalogClock a sobre.
 *
 * Com un GMT de debò: l'agulla taronja marca l'HORA LOCAL en 24 h sobre una esfera fixa
 * i invisible (24 a dalt, 12 a baix). L'esfera visible (números, punts i cercle
 * blau/vermell) és la que gira segons la diferència amb la segona zona: llegint on
 * apunta l'agulla sobre els números es veu l'hora de l'altra zona.
 */
object ClockRenderer {
    private val TILE = 0xFF0B0B0D.toInt()
    private val NIGHT = 0xFF1B3A8C.toInt()
    private val DAY = 0xFFB3262E.toInt()
    private val AMBER = 0xFFFFB020.toInt()
    private val WHITE = 0xFFF2F2F2.toInt()
    private val GREY = 0xFF8E8E93.toInt()

    private const val BRAND = "FWCo" // Federicu Watch Company

    /** Radi de les cantonades del widget (sobre 400). Abans 92; més petit = cantonades més quadrades. */
    private const val CORNER = 24f

    fun render(size: Int, zone: ZoneId, label: String): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.scale(size / 400f, size / 400f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fons
        p.color = TILE
        c.drawRoundRect(RectF(0f, 0f, 400f, 400f), CORNER, CORNER, p)

        // Hora local i gir de l'esfera visible. La diferència (segona zona - local) ve dels
        // desplaçaments actuals (així compta l'horari d'estiu) i pot ser fraccionària (p. ex. l'Índia).
        // Una zona 1 h per davant fa girar l'esfera 15° en sentit antihorari.
        val nowInstant = Instant.now()
        val localZone = ZoneId.systemDefault()
        val local = ZonedDateTime.ofInstant(nowInstant, localZone)
        val diffSeconds = zone.rules.getOffset(nowInstant).totalSeconds -
            localZone.rules.getOffset(nowInstant).totalSeconds
        val dialRot = -diffSeconds / 3600f * 15f

        // Cercle fi per FORA dels números: meitat superior = nit (blau), inferior = dia (vermell).
        // Forma part de l'esfera giratòria.
        c.save()
        c.rotate(dialRot, 200f, 200f)
        val ring = Paint(Paint.ANTI_ALIAS_FLAG)
        ring.style = Paint.Style.STROKE
        ring.strokeWidth = 7f
        val outer = RectF(12f, 12f, 388f, 388f)
        ring.color = NIGHT
        c.drawArc(outer, 181f, 178f, false, ring)
        ring.color = DAY
        c.drawArc(outer, 1f, 178f, false, ring)
        c.restore()

        // Anell fi interior (límit de l'esfera)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.5f
        p.color = 0xFF5A5A5E.toInt()
        c.drawCircle(200f, 200f, 152f, p)
        p.style = Paint.Style.FILL

        // Números de 24 h i punts per les hores senars. Giren amb l'esfera, però cada xifra
        // es manté dreta perquè es pugui llegir sempre (només canvia la posició).
        val nums = Paint(Paint.ANTI_ALIAS_FLAG)
        nums.color = WHITE
        nums.textSize = 21f
        nums.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        nums.textAlign = Paint.Align.CENTER
        val fm = nums.fontMetrics
        val dy = -(fm.ascent + fm.descent) / 2f
        val dot = Paint(Paint.ANTI_ALIAS_FLAG)
        dot.color = WHITE
        for (h in 0 until 24) {
            val a = Math.toRadians(h * 15.0 + dialRot)
            val x = (200.0 + 170.0 * sin(a)).toFloat()
            val y = (200.0 - 170.0 * cos(a)).toFloat()
            if (h % 2 == 0) {
                c.drawText(if (h == 0) "24" else h.toString(), x, y + dy, nums)
            } else {
                c.drawCircle(x, y, 2.6f, dot)
            }
        }

        // Marques de minuts
        val tick = Paint(Paint.ANTI_ALIAS_FLAG)
        tick.color = GREY
        tick.strokeWidth = 1.6f
        for (i in 0 until 60) {
            if (i % 5 == 0) continue
            c.save()
            c.rotate(i * 6f, 200f, 200f)
            c.drawLine(200f, 53f, 200f, 60f, tick)
            c.restore()
        }

        // Índexs d'hora (el de les 3 queda substituït per la finestreta de la data)
        val bar = Paint(Paint.ANTI_ALIAS_FLAG)
        bar.color = WHITE
        for (i in 0 until 12) {
            if (i == 3) continue
            c.save()
            c.rotate(i * 30f, 200f, 200f)
            if (i == 0) {
                val tri = Path()
                tri.moveTo(186f, 62f)
                tri.lineTo(214f, 62f)
                tri.lineTo(200f, 96f)
                tri.close()
                c.drawPath(tri, bar)
            } else if (i % 3 == 0) {
                c.drawRoundRect(RectF(192f, 62f, 208f, 106f), 3f, 3f, bar)
            } else {
                c.drawRoundRect(RectF(194f, 62f, 206f, 100f), 3f, 3f, bar)
            }
            c.restore()
        }

        // Finestreta de la data (a les 3): fons negre, marc blanc fi, xifres blanques, estreta
        val win = RectF(279f, 185f, 317f, 215f)
        p.color = 0xFF000000.toInt()
        c.drawRoundRect(win, 3f, 3f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.8f
        p.color = WHITE
        c.drawRoundRect(win, 3f, 3f, p)
        p.style = Paint.Style.FILL
        val day = Paint(Paint.ANTI_ALIAS_FLAG)
        day.color = WHITE
        day.textSize = 23f
        day.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        day.textAlign = Paint.Align.CENTER
        val dfm = day.fontMetrics
        c.drawText(LocalDate.now().dayOfMonth.toString(), win.centerX(), win.centerY() - (dfm.ascent + dfm.descent) / 2f, day)

        // Textos: la marca a dalt (a les 12) i la segona zona horària (ambre) a baix (a les 6)
        val t = Paint(Paint.ANTI_ALIAS_FLAG)
        t.textAlign = Paint.Align.CENTER
        t.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        t.letterSpacing = 0.08f
        t.textSize = 17f
        t.color = AMBER
        c.drawText(label.uppercase().take(13), 200f, 282f, t)

        val brand = Paint(Paint.ANTI_ALIAS_FLAG)
        brand.textAlign = Paint.Align.CENTER
        brand.typeface = Typeface.create("serif", Typeface.NORMAL)
        brand.textSize = 25f
        brand.textScaleX = 0.78f   // serif allargada, sobria
        brand.letterSpacing = 0.14f
        brand.color = 0xFFC4C4C9.toInt()
        c.drawText(BRAND, 200f, 138f, brand)

        // Agulla GMT (24 h): una volta cada 24 h, marca l'hora LOCAL respecte a l'esfera
        // fixa invisible (24 a dalt, 12 a baix). Els números, que giren, en donen l'hora de l'altra zona.
        val angle = (local.hour + local.minute / 60f) / 24f * 360f
        c.save()
        c.rotate(angle, 200f, 200f)
        val line = Paint(Paint.ANTI_ALIAS_FLAG)
        line.color = AMBER
        line.style = Paint.Style.STROKE
        line.strokeWidth = 5f
        line.strokeCap = Paint.Cap.ROUND
        c.drawLine(200f, 226f, 200f, 96f, line)
        val arrow = Path()
        arrow.moveTo(200f, 52f)
        arrow.lineTo(187f, 96f)
        arrow.lineTo(213f, 96f)
        arrow.close()
        p.color = AMBER
        c.drawPath(arrow, p)
        c.restore()

        return bmp
    }
}
