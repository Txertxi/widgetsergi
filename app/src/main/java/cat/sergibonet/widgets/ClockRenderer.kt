package cat.sergibonet.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Dibuixa la capa de fons del rellotge GMT (tot en coordenades 0..400):
 * bisell de 24 h (nit/dia), índexs, finestreta de data i l'agulla GMT.
 * Les agulles d'hores, minuts i segons les posa AnalogClock a sobre.
 */
object ClockRenderer {
    private val TILE = 0xFF0B0B0D.toInt()
    private val NIGHT = 0xFF1B3A8C.toInt()
    private val DAY = 0xFFB3262E.toInt()
    private val AMBER = 0xFFFFB020.toInt()
    private val WHITE = 0xFFF2F2F2.toInt()
    private val GREY = 0xFF8E8E93.toInt()

    fun render(size: Int, zone: ZoneId, label: String): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.scale(size / 400f, size / 400f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fons
        p.color = TILE
        c.drawRoundRect(RectF(0f, 0f, 400f, 400f), 92f, 92f, p)

        // Bisell de 24 h: meitat superior = nit (blau), inferior = dia (vermell)
        val outer = RectF(10f, 10f, 390f, 390f)
        p.color = NIGHT
        c.drawArc(outer, 180f, 180f, true, p)
        p.color = DAY
        c.drawArc(outer, 0f, 180f, true, p)
        p.color = TILE
        c.drawCircle(200f, 200f, 152f, p)
        // petites separacions entre nit i dia
        p.style = Paint.Style.STROKE
        p.strokeWidth = 2.5f
        c.drawLine(10f, 200f, 48f, 200f, p)
        c.drawLine(352f, 200f, 390f, 200f, p)
        // anell fi interior
        p.color = 0xFF5A5A5E.toInt()
        p.strokeWidth = 1.5f
        c.drawCircle(200f, 200f, 152f, p)
        p.style = Paint.Style.FILL

        // Números del bisell (24 a dalt, 12 a baix) i punts per les hores senars
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
            val a = Math.toRadians(h * 15.0)
            val x = (200.0 + 171.0 * sin(a)).toFloat()
            val y = (200.0 - 171.0 * cos(a)).toFloat()
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

        // Finestreta de la data (a les 3)
        val win = RectF(268f, 184f, 328f, 216f)
        p.color = 0xFFF4F4F2.toInt()
        c.drawRoundRect(win, 4f, 4f, p)
        val day = Paint(Paint.ANTI_ALIAS_FLAG)
        day.color = 0xFF111111.toInt()
        day.textSize = 25f
        day.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        day.textAlign = Paint.Align.CENTER
        val dfm = day.fontMetrics
        c.drawText(LocalDate.now().dayOfMonth.toString(), win.centerX(), win.centerY() - (dfm.ascent + dfm.descent) / 2f, day)

        // Textos: zona de l'agulla GMT (ambre) i "GMT" a baix
        val t = Paint(Paint.ANTI_ALIAS_FLAG)
        t.textAlign = Paint.Align.CENTER
        t.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        t.letterSpacing = 0.08f
        t.textSize = 17f
        t.color = AMBER
        val shown = label.uppercase().take(13)
        c.drawText(shown, 200f, 138f, t)
        t.textSize = 15f
        t.color = GREY
        c.drawText("GMT", 200f, 282f, t)

        // Agulla GMT (24 h): una volta cada 24 h, apunta al bisell
        val z = ZonedDateTime.now(zone)
        val angle = (z.hour + z.minute / 60f) / 24f * 360f
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
