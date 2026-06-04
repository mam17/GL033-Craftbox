package com.example.myapplication.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.withStyledAttributes
import com.example.myapplication.R

class RoundImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private var enableScaleAnim: Boolean = true
    private var cornerRadius: Float = 0f
    private var isOval: Boolean = false
    private var bgColor: Int = Color.TRANSPARENT
    private var strokeEnable: Boolean = false
    private var strokeColor: Int = Color.TRANSPARENT
    private var strokeWidth: Float = 0f
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val path = Path()
    private val rectF = RectF()

    init {
        isClickable = true
        scaleType = ScaleType.CENTER_CROP

        attrs?.let {
            context.withStyledAttributes(it, R.styleable.RoundImageView) {
                cornerRadius = getDimension(R.styleable.RoundImageView_cornerRadius, 0f)
                isOval = getBoolean(R.styleable.RoundImageView_isOval, false)
                bgColor = getColor(R.styleable.RoundImageView_bgColor, Color.TRANSPARENT)
                enableScaleAnim = getBoolean(R.styleable.RoundImageView_enableScaleAnim, true)
                strokeEnable = getBoolean(R.styleable.RoundImageView_strokeEnable, false)
                strokeColor = getColor(R.styleable.RoundImageView_strokeColor, Color.TRANSPARENT)
                strokeWidth = getDimension(R.styleable.RoundImageView_strokeWidth, 0f)

                if (hasValue(R.styleable.RoundImageView_iconTint)) {
                    val tintColor = getColor(R.styleable.RoundImageView_iconTint, Color.TRANSPARENT)
                    setColorFilter(tintColor, PorterDuff.Mode.SRC_IN)
                }
            }
        }
        updatePath()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        updatePath()
    }

    private fun updatePath() {
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        path.reset()
        if (isOval) {
            path.addOval(rectF, Path.Direction.CW)
        } else {
            path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW)
        }
    }

    fun setBitmap(bitmap: Bitmap) {
        setImageBitmap(bitmap)
    }

    fun setCornerRadius(radius: Float) {
        this.cornerRadius = radius
        updatePath()
        invalidate()
    }

    fun setOval(enable: Boolean) {
        this.isOval = enable
        updatePath()
        invalidate()
    }

    fun setBgColor(color: Int) {
        this.bgColor = color
        invalidate()
    }

    fun setStroke(enable: Boolean, color: Int, width: Float) {
        this.strokeEnable = enable
        this.strokeColor = color
        this.strokeWidth = width
        invalidate()
    }

    fun setEnableScaleAnim(enable: Boolean) {
        this.enableScaleAnim = enable
    }

    fun setIconTint(color: Int) {
        setColorFilter(color, PorterDuff.Mode.SRC_IN)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        if (width > 0 && height > 0) {
            canvas.clipPath(path)
            if (bgColor != Color.TRANSPARENT) {
                paint.color = bgColor
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }
        super.onDraw(canvas)
        if (strokeEnable && strokeWidth > 0f && strokeColor != Color.TRANSPARENT) {
            strokePaint.style = Paint.Style.STROKE
            strokePaint.color = strokeColor
            strokePaint.strokeWidth = strokeWidth
            canvas.drawPath(path, strokePaint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (enableScaleAnim && isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> startScaleAnim(0.95f)
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> startScaleAnim(1f)
            }
        }

        return if (hasOnClickListeners()) {
            super.onTouchEvent(event)
        } else {
            false
        }
    }

    private fun startScaleAnim(scale: Float) {
        animate()
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(120)
            .start()
    }
}