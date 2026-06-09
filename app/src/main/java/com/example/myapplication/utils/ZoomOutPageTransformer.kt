package com.example.myapplication.utils

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class ZoomOutPageTransformer(private val nextItemTranslationX: Int) : ViewPager2.PageTransformer {
    private val MIN_ALPHA = 0.72f
    private val MIN_SCALE = 0.9f

    override fun transformPage(view: View, position: Float) {
        val absPosition = abs(position)

        if (position < -1) {
            view.alpha = 0f
        } else if (position <= 1) {
            val scale = MIN_SCALE + (1 - MIN_SCALE) * (1 - absPosition)
            val alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - absPosition)

            view.alpha = alpha
            view.scaleY = scale
            view.scaleX = scale

            view.translationX = -position * nextItemTranslationX
        } else {
            view.alpha = 0f
        }
    }
}
