package cat.sergibonet.widgets

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import java.time.LocalDate
import java.time.YearMonth

/** Dibuixa el calendari sencer en un bitmap (català, setmana de dilluns a diumenge, festius en colors). */
object CalendarRenderer {
    private val MONTHS = arrayOf(
        "gener", "febrer", "març", "abril", "maig", "juny",
        "juliol", "agost", "setembre", "octubre", "novembre", "desembre"
    )
    private val MON3 = arrayOf("GEN", "FEB", "MAR", "ABR", "MAI", "JUN", "JUL", "AGO", "SET", "OCT", "NOV", "DES")
    private val DAYS = arrayOf("dilluns", "dimarts", "dimecres", "dijous", "divendres", "dissabte", "diumenge")
    private val HEAD = arrayOf("Dl", "Dt", "Dc", "Dj", "Dv", "Ds", "Dg")

    private val BG = 0xFF1C1C1E.toInt()
    private val WHITE = 0xFFFFFFFF.toInt()
    private val GREY = 0xFFB0B0B5.toInt()
    private val YELLOW = 0xFFFFD60A.toInt()
    private val DARK = 0xFF1C1C1E.toInt()
    private val TILE_TOP = 0xFF3C3C3F.toInt()
    private val TILE_BOTTOM = 0xFF2B2B2E.toInt()
    private val SPLIT = 0xFF101011.toInt()

    // Colors dels festius
    private val C_STATE = 0xFFFF5A5F.toInt()      // estatal
    private val C_CATALONIA = 0xFF4FC3F7.toInt()  // Catalunya
    private val C_LOCAL = 0xFF6EE7A0.toInt()      // local

    fun levelColor(l: Level): Int = when (l) {
        Level.STATE -> C_STATE
        Level.CATALONIA -> C_CATALONIA
        Level.LOCAL -> C_LOCAL
    }

    private fun textPaint(color: Int, size: Float, face: Typeface): Paint {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        p.textSize = size
        p.typeface = face
        p.textAlign = Paint.Align.CENTER
        return p
    }

    private fun medium() = Typeface.create("sans-serif-medium", Typeface.NORMAL)
    private fun condensedBold() = Typeface.create("sans-serif-condensed", Typeface.BOLD)

    /** Baseline per centrar verticalment el text al voltant de cy. */
    private fun baseline(p: Paint, cy: Float): Float {
        val fm = p.fontMetrics
        return cy - (fm.ascent + fm.descent) / 2f
    }

    fun render(context: Context, w: Int, h: Int, monthOffset: Int, today: LocalDate): Bitmap {
        val holidays = Holidays.load(context)
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val W = w.toFloat()
        val H = h.toFloat()
        val fill = Paint(Paint.ANTI_ALIAS_FLAG)

        // Targeta
        fill.color = BG
        val cr = minOf(W, H) * 0.14f
        c.drawRoundRect(RectF(0f, 0f, W, H), cr, cr, fill)

        // ---------- Costat esquerre: "flip" mes + dia, dia de la setmana, festiu
        val lx = W * 0.045f
        val lw = W * 0.43f
        val tileTop = H * 0.09f
        val tileH = H * 0.43f
        val gap = lw * 0.05f
        val monthW = (lw - gap) * 0.62f
        val dayW = lw - gap - monthW
        flipTile(c, RectF(lx, tileTop, lx + monthW, tileTop + tileH), MON3[today.monthValue - 1])
        flipTile(c, RectF(lx + monthW + gap, tileTop, lx + monthW + gap + dayW, tileTop + tileH), today.dayOfMonth.toString())

        val wd = textPaint(WHITE, H * 0.09f, medium())
        wd.textAlign = Paint.Align.LEFT
        c.drawText(DAYS[today.dayOfWeek.value - 1].replaceFirstChar { it.uppercase() }, lx, H * 0.665f, wd)

        val todays = Holidays.strongest(holidays[today])
        val infoText: String
        val infoColor: Int
        if (todays != null) {
            infoText = "Avui: " + todays.label()
            infoColor = levelColor(todays.level)
        } else {
            val n = Holidays.next(holidays, today.plusDays(1))
            if (n != null) {
                infoText = "${n.date.dayOfMonth} ${MON3[n.date.monthValue - 1].lowercase()} · ${n.label()}"
                infoColor = levelColor(n.level)
            } else {
                infoText = "Cap festiu proper"
                infoColor = GREY
            }
        }
        val tp = TextPaint(Paint.ANTI_ALIAS_FLAG)
        tp.color = infoColor
        tp.textSize = H * 0.07f
        tp.typeface = medium()
        val layout = StaticLayout.Builder.obtain(infoText, 0, infoText.length, tp, lw.toInt())
            .setMaxLines(2)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        c.save()
        c.translate(lx, H * 0.72f)
        layout.draw(c)
        c.restore()

        // ---------- Costat dret: mes
        val ym = YearMonth.from(today).plusMonths(monthOffset.toLong())
        val title = MONTHS[ym.monthValue - 1].replaceFirstChar { it.uppercase() } + " " + ym.year
        val headerCy = H * 0.085f
        val titleP = textPaint(WHITE, H * 0.085f, medium())
        c.drawText(title, W * 0.74f, baseline(titleP, headerCy), titleP)
        val chev = textPaint(YELLOW, H * 0.14f, medium())
        c.drawText("‹", W * 0.54f, baseline(chev, headerCy), chev)
        c.drawText("›", W * 0.96f, baseline(chev, headerCy), chev)

        val gx0 = W * 0.515f
        val gx1 = W * 0.965f
        val colW = (gx1 - gx0) / 7f
        val top0 = H * 0.175f
        val rowH = (H * 0.965f - top0) / 7f

        // Capçalera de dies
        val hp = textPaint(GREY, rowH * 0.5f, Typeface.create("sans-serif-medium", Typeface.BOLD))
        for (i in 0..6) {
            hp.color = if (i >= 5) YELLOW else WHITE
            c.drawText(HEAD[i], gx0 + colW * (i + 0.5f), baseline(hp, top0 + rowH * 0.5f), hp)
        }

        // Dies
        val np = textPaint(WHITE, rowH * 0.52f, medium())
        val ring = Paint(Paint.ANTI_ALIAS_FLAG)
        ring.style = Paint.Style.STROKE
        ring.strokeWidth = rowH * 0.07f
        val startOffset = ym.atDay(1).dayOfWeek.value - 1 // dilluns = 0
        val rad = minOf(colW, rowH) * 0.47f
        for (d in 1..ym.lengthOfMonth()) {
            val date = ym.atDay(d)
            val idx = startOffset + d - 1
            val row = 1 + idx / 7
            val col = idx % 7
            val cx = gx0 + colW * (col + 0.5f)
            val cy = top0 + rowH * (row + 0.5f)
            val hol = Holidays.strongest(holidays[date])
            val isToday = date == today
            val weekend = date.dayOfWeek.value >= 6

            if (isToday) {
                fill.color = YELLOW
                c.drawCircle(cx, cy, rad, fill)
            }
            if (hol != null) {
                ring.color = levelColor(hol.level)
                c.drawCircle(cx, cy, rad - ring.strokeWidth / 2f, ring)
            }
            np.color = when {
                isToday -> DARK
                hol != null -> levelColor(hol.level)
                weekend -> YELLOW
                else -> WHITE
            }
            np.typeface = if (isToday || hol != null) Typeface.create("sans-serif-medium", Typeface.BOLD) else medium()
            c.drawText(d.toString(), cx, baseline(np, cy), np)
        }
        return bmp
    }

    private fun flipTile(c: Canvas, r: RectF, text: String) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        val rr = r.height() * 0.10f
        p.color = TILE_TOP
        c.drawRoundRect(r, rr, rr, p)
        c.save()
        c.clipRect(r.left, r.centerY(), r.right, r.bottom)
        p.color = TILE_BOTTOM
        c.drawRoundRect(r, rr, rr, p)
        c.restore()

        val tp = textPaint(WHITE, r.height() * 0.8f, condensedBold())
        val tw = tp.measureText(text)
        val maxW = r.width() * 0.86f
        if (tw > maxW) tp.textSize = tp.textSize * maxW / tw
        val b = Rect()
        tp.getTextBounds(text, 0, text.length, b)
        c.drawText(text, r.centerX(), r.centerY() - b.exactCenterY(), tp)

        // Línia de la "xapa" del flip
        p.color = SPLIT
        c.drawRect(r.left, r.centerY() - r.height() * 0.012f, r.right, r.centerY() + r.height() * 0.012f, p)
    }
}
