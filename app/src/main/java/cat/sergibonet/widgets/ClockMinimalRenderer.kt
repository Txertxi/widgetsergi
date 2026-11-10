package cat.sergibonet.widgets

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.cos
import kotlin.math.sin

/**
 * Versió minimalista del rellotge GMT: sense bisell de colors, sense lletres,
 * sense finestreta de data i amb punts en lloc d'índexs. Conserva els números
 * de 24 h i l'agulla GMT. Coordenades 0..400.
 */
object ClockMinimalRenderer {
    private val TILE = 0xFF0B0B0D.toInt()
    private val AMBER = 0xFFFFB020.toInt()
    private val WHITE = 0xFFF2F2F2.toInt()
    private val NUM = 0xFFC9C9CE.toInt()

    /** Radi de les cantonades del widget (sobre 400). Abans 92. */
    private const val CORNER = 24f

    fun render(size: Int, zone: ZoneId): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.scale(size / 400f, size / 400f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fons
        p.color = TILE
        c.drawRoundRect(RectF(0f, 0f, 400f, 400f), CORNER, CORNER, p)

        // Anell fi
        p.style = Paint.Style.STROKE
        p.strokeWidth = 1.2f
        p.color = 0xFF3A3A3E.toInt()
        c.drawCircle(200f, 200f, 150f, p)
        p.style = Paint.Style.FILL

        // Números de 24 h (24 a dalt, 12 a baix) i puntets per les hores senars
        val nums = Paint(Paint.ANTI_ALIAS_FLAG)
        nums.color = NUM
        nums.textSize = 20f
        nums.typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        nums.textAlign = Paint.Align.CENTER
        val fm = nums.fontMetrics
        val dy = -(fm.ascent + fm.descent) / 2f
        val small = Paint(Paint.ANTI_ALIAS_FLAG)
        small.color = NUM
        for (h in 0 until 24) {
            val a = Math.toRadians(h * 15.0)
            val x = (200.0 + 172.0 * sin(a)).toFloat()
            val y = (200.0 - 172.0 * cos(a)).toFloat()
            if (h % 2 == 0) {
                c.drawText(if (h == 0) "24" else h.toString(), x, y + dy, nums)
            } else {
                c.drawCircle(x, y, 2f, small)
            }
        }

        // Agulla GMT (24 h), fina i amb punta de fletxa
        val z = ZonedDateTime.now(zone)
        val angle = (z.hour + z.minute / 60f) / 24f * 360f
        c.save()
        c.rotate(angle, 200f, 200f)
        val line = Paint(Paint.ANTI_ALIAS_FLAG)
        line.color = AMBER
        line.style = Paint.Style.STROKE
        line.strokeWidth = 3f
        line.strokeCap = Paint.Cap.ROUND
        c.drawLine(200f, 224f, 200f, 90f, line)
        val arrow = Path()
        arrow.moveTo(200f, 56f)
        arrow.lineTo(191f, 92f)
        arrow.lineTo(209f, 92f)
        arrow.close()
        p.color = AMBER
        c.drawPath(arrow, p)
        c.restore()

        return bmp
    }
}
