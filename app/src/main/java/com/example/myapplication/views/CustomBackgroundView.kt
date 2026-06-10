package com.example.myapplication.views

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import com.example.myapplication.R

/**
 * CustomBackgroundView
 *
 * Hỗ trợ:
 * - Background màu đơn hoặc gradient nhiều màu
 * - Background bằng Bitmap
 * - Blur background (bật/tắt + điều chỉnh độ blur)
 * - Bo góc (corner radius)
 * - Viền stroke (màu, độ dày, bật/tắt)
 *
 * Tất cả tính năng có enable đều mặc định = false (tắt).
 */
class CustomBackgroundView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // ─── Enums ────────────────────────────────────────────────────────────────

    enum class GradientOrientation {
        LEFT_RIGHT,
        TOP_BOTTOM,
        TL_BR,   // Top-Left → Bottom-Right
        TR_BL    // Top-Right → Bottom-Left
    }

    // ─── Paint & Path ─────────────────────────────────────────────────────────

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val clipPath = Path()
    private val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    // ─── Corner Radius ────────────────────────────────────────────────────────

    /** Bo tất cả 4 góc cùng một giá trị (px). */
    var cornerRadius: Float = 0f
        set(value) { field = value.coerceAtLeast(0f); invalidate() }

    /** Bo từng góc riêng lẻ (px): topLeft, topRight, bottomRight, bottomLeft */
    var cornerRadii: FloatArray? = null
        set(value) {
            require(value == null || value.size == 4) { "cornerRadii phải có đúng 4 phần tử" }
            field = value
            invalidate()
        }

    // ─── Solid Color ──────────────────────────────────────────────────────────

    @ColorInt
    var customSolidColor: Int = Color.WHITE
        set(value) { field = value; gradientColors = null; invalidate() }

    // ─── Gradient ─────────────────────────────────────────────────────────────

    var gradientColors: IntArray? = null
        set(value) { field = value; invalidate() }

    var gradientOrientation: GradientOrientation = GradientOrientation.LEFT_RIGHT
        set(value) { field = value; invalidate() }

    /** Vị trí tương đối của từng màu gradient (0f–1f). Null = phân bổ đều. */
    var gradientPositions: FloatArray? = null
        set(value) { field = value; invalidate() }

    // ─── Bitmap Background ────────────────────────────────────────────────────

    var backgroundBitmap: Bitmap? = null
        set(value) {
            field = value
            blurredBitmap = null   // reset cache
            invalidate()
        }

    /** ScaleType cho bitmap: CENTER, CENTER_CROP, CENTER_INSIDE, FIT_CENTER, FIT_START, FIT_END, FIT_XY. */
    enum class BitmapScaleType { CENTER, CENTER_CROP, CENTER_INSIDE, FIT_CENTER, FIT_START, FIT_END, FIT_XY }

    var bitmapScaleType: BitmapScaleType = BitmapScaleType.CENTER_CROP
        set(value) { field = value; invalidate() }

    // ─── Blur ─────────────────────────────────────────────────────────────────

    var blurEnabled: Boolean = false
        set(value) { field = value; blurredBitmap = null; invalidate() }

    /** Độ mờ blur: 1f – 25f (giới hạn của RenderScript). */
    var blurRadius: Float = 10f
        set(value) {
            field = value.coerceIn(1f, 25f)
            blurredBitmap = null
            invalidate()
        }

    private var blurredBitmap: Bitmap? = null
    private var lastBlurSourceBitmap: Bitmap? = null

    // ─── Stroke ───────────────────────────────────────────────────────────────

    var strokeEnabled: Boolean = false
        set(value) { field = value; invalidate() }

    @ColorInt
    var strokeColor: Int = Color.BLACK
        set(value) { field = value; invalidate() }

    var strokeWidth: Float = 4f
        set(value) { field = value.coerceAtLeast(0f); invalidate() }

    // ─── Overlay ──────────────────────────────────────────────────────────────

    @ColorInt
    var overlayColor: Int = Color.TRANSPARENT
        set(value) { field = value; invalidate() }

    // ─── Init from XML ────────────────────────────────────────────────────────

    init {
        if (attrs != null) {
            val ta = context.obtainStyledAttributes(attrs, R.styleable.CustomBackgroundView)
            try {
                // Corner
                cornerRadius = ta.getDimension(R.styleable.CustomBackgroundView_cbv_cornerRadius, 0f)

                // Solid color
                customSolidColor = ta.getColor(R.styleable.CustomBackgroundView_cbv_solidColor, Color.WHITE)

                // Gradient colors (chỉ hỗ trợ startColor / endColor / centerColor qua XML)
                val startColor = ta.getColor(R.styleable.CustomBackgroundView_cbv_gradientStartColor, Int.MIN_VALUE)
                val endColor   = ta.getColor(R.styleable.CustomBackgroundView_cbv_gradientEndColor,   Int.MIN_VALUE)
                val centerColor= ta.getColor(R.styleable.CustomBackgroundView_cbv_gradientCenterColor, Int.MIN_VALUE)
                if (startColor != Int.MIN_VALUE && endColor != Int.MIN_VALUE) {
                    gradientColors = if (centerColor != Int.MIN_VALUE)
                        intArrayOf(startColor, centerColor, endColor)
                    else
                        intArrayOf(startColor, endColor)
                }

                val orientOrdinal = ta.getInt(R.styleable.CustomBackgroundView_cbv_gradientOrientation, 0)
                gradientOrientation = GradientOrientation.entries[orientOrdinal]

                // Bitmap ScaleType
                val scaleTypeOrdinal = ta.getInt(R.styleable.CustomBackgroundView_cbv_bitmapScaleType, 1) // Default CENTER_CROP
                bitmapScaleType = BitmapScaleType.entries[scaleTypeOrdinal]

                // Blur
                blurEnabled = ta.getBoolean(R.styleable.CustomBackgroundView_cbv_blurEnabled, false)
                blurRadius   = ta.getFloat(R.styleable.CustomBackgroundView_cbv_blurRadius, 10f).coerceIn(1f, 25f)

                // Stroke
                strokeEnabled = ta.getBoolean(R.styleable.CustomBackgroundView_cbv_strokeEnabled, false)
                strokeColor   = ta.getColor(R.styleable.CustomBackgroundView_cbv_strokeColor, Color.BLACK)
                strokeWidth   = ta.getDimension(R.styleable.CustomBackgroundView_cbv_strokeWidth, 4f)

                // Bitmap từ XML src (drawable)
                val drawable = ta.getDrawable(R.styleable.CustomBackgroundView_cbv_backgroundBitmap)
                if (drawable is BitmapDrawable) backgroundBitmap = drawable.bitmap

            } finally {
                ta.recycle()
            }
        }

        // Tắt background mặc định của View để tránh vẽ chồng
        setWillNotDraw(false)
    }

    // ─── Measure ──────────────────────────────────────────────────────────────

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        blurredBitmap = null // kích thước thay đổi → rebuild
    }

    // ─── Draw ─────────────────────────────────────────────────────────────────

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return

        val w = width.toFloat()
        val h = height.toFloat()

        // 1. Tính toán stroke inset để vẽ bên trong bound
        val halfStroke = if (strokeEnabled) strokeWidth / 2f else 0f
        val left   = halfStroke
        val top    = halfStroke
        val right  = w - halfStroke
        val bottom = h - halfStroke

        // 2. Xây dựng clipPath theo radius
        clipPath.reset()
        val radii = buildRadiiArray(left, top, right, bottom)
        clipPath.addRoundRect(RectF(left, top, right, bottom), radii, Path.Direction.CW)

        // 3. Clip để bo góc cho toàn bộ nội dung
        canvas.save()
        canvas.clipPath(clipPath)

        val drawRect = RectF(left, top, right, bottom)

        // 4. Vẽ background bitmap (nếu có)
        val bmp = backgroundBitmap
        if (bmp != null && !bmp.isRecycled) {
            val finalBmp = if (blurEnabled) getBlurredBitmap(bmp) else bmp
            val srcRect  = Rect(0, 0, finalBmp.width, finalBmp.height)
            val dstRect  = computeBitmapDstRect(finalBmp, drawRect)
            canvas.drawBitmap(finalBmp, srcRect, dstRect, bitmapPaint)
        } else {
            // 5. Vẽ background màu / gradient
            val colors = gradientColors
            if (colors != null && colors.size >= 2) {
                backgroundPaint.shader = buildLinearGradient(colors, drawRect)
                backgroundPaint.color  = Color.WHITE
            } else {
                backgroundPaint.shader = null
                backgroundPaint.color  = customSolidColor
            }
            backgroundPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, backgroundPaint)
        }

        // 5.5 Vẽ Overlay (nếu có)
        if (Color.alpha(overlayColor) > 0) {
            backgroundPaint.shader = null
            backgroundPaint.color  = overlayColor
            backgroundPaint.style  = Paint.Style.FILL
            canvas.drawRoundRect(drawRect, cornerRadius, cornerRadius, backgroundPaint)
        }

        canvas.restore()

        // 6. Vẽ stroke (trên cùng, không bị clip)
        if (strokeEnabled && strokeWidth > 0f) {
            strokePaint.color       = strokeColor
            strokePaint.strokeWidth = strokeWidth
            val strokeRadii = buildRadiiArray(left, top, right, bottom)
            val strokePath  = Path().apply {
                addRoundRect(RectF(left, top, right, bottom), strokeRadii, Path.Direction.CW)
            }
            canvas.drawPath(strokePath, strokePaint)
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Trả về mảng 8 float cho RectF radii:
     * [TL-x, TL-y, TR-x, TR-y, BR-x, BR-y, BL-x, BL-y]
     */
    private fun buildRadiiArray(
        @Suppress("UNUSED_PARAMETER") l: Float,
        @Suppress("UNUSED_PARAMETER") t: Float,
        @Suppress("UNUSED_PARAMETER") r: Float,
        @Suppress("UNUSED_PARAMETER") b: Float
    ): FloatArray {
        val radii = cornerRadii
        return if (radii != null && radii.size == 4) {
            floatArrayOf(
                radii[0], radii[0],
                radii[1], radii[1],
                radii[2], radii[2],
                radii[3], radii[3]
            )
        } else {
            FloatArray(8) { cornerRadius }
        }
    }

    /** Tạo LinearGradient theo orientation */
    private fun buildLinearGradient(colors: IntArray, rect: RectF): LinearGradient {
        val (x0, y0, x1, y1) = when (gradientOrientation) {
            GradientOrientation.LEFT_RIGHT -> floatArrayOf(rect.left,  rect.top,    rect.right, rect.top   )
            GradientOrientation.TOP_BOTTOM -> floatArrayOf(rect.left,  rect.top,    rect.left,  rect.bottom)
            GradientOrientation.TL_BR      -> floatArrayOf(rect.left,  rect.top,    rect.right, rect.bottom)
            GradientOrientation.TR_BL      -> floatArrayOf(rect.right, rect.top,    rect.left,  rect.bottom)
        }
        return LinearGradient(x0, y0, x1, y1, colors, gradientPositions, Shader.TileMode.CLAMP)
    }

    /** Tính toán RectF để vẽ bitmap theo scaleType */
    private fun computeBitmapDstRect(bmp: Bitmap, viewRect: RectF): RectF {
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()
        val vw = viewRect.width()
        val vh = viewRect.height()

        return when (bitmapScaleType) {
            BitmapScaleType.CENTER -> {
                val left = viewRect.left + (vw - bw) / 2f
                val top  = viewRect.top  + (vh - bh) / 2f
                RectF(left, top, left + bw, top + bh)
            }
            BitmapScaleType.CENTER_CROP -> {
                val scale = maxOf(vw / bw, vh / bh)
                val dw = bw * scale
                val dh = bh * scale
                RectF(
                    viewRect.left - (dw - vw) / 2f,
                    viewRect.top  - (dh - vh) / 2f,
                    viewRect.left + (dw + vw) / 2f - (dw - vw) / 2f,
                    viewRect.top  + (dh + vh) / 2f - (dh - vh) / 2f
                )
            }
            BitmapScaleType.CENTER_INSIDE -> {
                val scale = if (bw <= vw && bh <= vh) 1f else minOf(vw / bw, vh / bh)
                val dw = bw * scale
                val dh = bh * scale
                val left = viewRect.left + (vw - dw) / 2f
                val top  = viewRect.top  + (vh - dh) / 2f
                RectF(left, top, left + dw, top + dh)
            }
            BitmapScaleType.FIT_CENTER -> {
                val scale = minOf(vw / bw, vh / bh)
                val dw = bw * scale
                val dh = bh * scale
                val left = viewRect.left + (vw - dw) / 2f
                val top  = viewRect.top  + (vh - dh) / 2f
                RectF(left, top, left + dw, top + dh)
            }
            BitmapScaleType.FIT_START -> {
                val scale = minOf(vw / bw, vh / bh)
                val dw = bw * scale
                val dh = bh * scale
                RectF(viewRect.left, viewRect.top, viewRect.left + dw, viewRect.top + dh)
            }
            BitmapScaleType.FIT_END -> {
                val scale = minOf(vw / bw, vh / bh)
                val dw = bw * scale
                val dh = bh * scale
                RectF(viewRect.right - dw, viewRect.bottom - dh, viewRect.right, viewRect.bottom)
            }
            BitmapScaleType.FIT_XY -> {
                RectF(viewRect.left, viewRect.top, viewRect.right, viewRect.bottom)
            }
        }
    }

    /** Trả về bitmap đã blur, cache lại nếu source không đổi */
    @Suppress("DEPRECATION")
    private fun getBlurredBitmap(source: Bitmap): Bitmap {
        if (blurredBitmap != null && lastBlurSourceBitmap == source) {
            return blurredBitmap!!
        }

        // Scale nhỏ lại để tăng tốc blur (RenderScript tối đa 25f tại full-res)
        val scaleFactor = 4
        val scaledW = (source.width  / scaleFactor).coerceAtLeast(1)
        val scaledH = (source.height / scaleFactor).coerceAtLeast(1)

        val scaledBitmap = Bitmap.createScaledBitmap(source, scaledW, scaledH, false)
        val outputBitmap = scaledBitmap.copy(Bitmap.Config.ARGB_8888, true)

        try {
            val rs    = RenderScript.create(context)
            val input = Allocation.createFromBitmap(rs, outputBitmap)
            val out   = Allocation.createTyped(rs, input.type)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            script.setRadius(blurRadius)
            script.setInput(input)
            script.forEach(out)
            out.copyTo(outputBitmap)

            rs.destroy()
        } catch (e: Exception) {
            // Fallback: trả về bitmap gốc nếu RenderScript không khả dụng
            return source
        }

        // Scale lại kích thước gốc
        val result = Bitmap.createScaledBitmap(outputBitmap, source.width, source.height, true)
        blurredBitmap = result
        lastBlurSourceBitmap = source
        return result
    }

    // ─── Convenience API ──────────────────────────────────────────────────────

    /** Đặt gradient từ vararg màu (tiện dụng hơn khi dùng code). */
    @JvmName("setGradientColorsVararg")
    fun setGradientColors(vararg colors: Int) {
        gradientColors = colors
    }

    /** Đặt stroke trong một lần gọi. */
    fun setStroke(enabled: Boolean, @ColorInt color: Int = strokeColor, widthPx: Float = strokeWidth) {
        strokeEnabled = enabled
        strokeColor   = color
        strokeWidth   = widthPx
        invalidate()
    }

    /** Đặt blur trong một lần gọi. */
    fun setBlur(enabled: Boolean, radius: Float = blurRadius) {
        blurEnabled = enabled
        blurRadius   = radius
    }
}
