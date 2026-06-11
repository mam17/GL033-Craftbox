package com.grl.sms_wa.utils

import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.ImageView
import androidx.core.graphics.toColorInt
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.views.CustomBackgroundView

object ThemeUiHelper {

    fun bindBackground(target: ImageView, theme: ThemeMessModel?) {
        if (theme == null) {
            target.setImageDrawable(null)
            target.setBackgroundColor(Color.TRANSPARENT)
            return
        }

        val backgroundValue = theme.pathBG
        if (backgroundValue.startsWith("#")) {
            target.setImageDrawable(null)
            target.setBackgroundColor(backgroundValue.toColorInt())
        } else {
            target.setBackgroundColor(Color.TRANSPARENT)
            with(ImageUtils) {
                target.loadFromPathAction(
                    path = "file:///android_asset/$backgroundValue",
                    isCenterCrop = true
                )
            }
        }
    }

    fun bindBackground(target: CustomBackgroundView, theme: ThemeMessModel?) {
        if (theme == null) {
            target.backgroundBitmap = null
            target.customSolidColor = Color.WHITE
            target.overlayColor = Color.TRANSPARENT
            target.blurEnabled = false
            return
        }

        // Overlay (alpha 0–255)
        val overlayAlpha = theme.overlayAlpha.coerceIn(0, 255)
        target.overlayColor = if (overlayAlpha > 0) Color.argb(overlayAlpha, 0, 0, 0)
                              else Color.TRANSPARENT

        // Blur (radius 0–25)
        val blur = theme.blurRadius.coerceIn(0, 25)
        target.blurEnabled = blur > 0
        if (blur > 0) target.blurRadius = blur.toFloat()

        val backgroundValue = theme.pathBG
        if (backgroundValue.startsWith("#")) {
            target.backgroundBitmap = null
            target.customSolidColor = backgroundValue.toColorInt()
            return
        }

        // Gradient orientation
        target.gradientOrientation = if (theme.isOrientationColorBG) {
            CustomBackgroundView.GradientOrientation.LEFT_RIGHT
        } else {
            CustomBackgroundView.GradientOrientation.TOP_BOTTOM
        }

        target.customSolidColor = Color.TRANSPARENT
        val isAsset = !backgroundValue.startsWith("content://") && !backgroundValue.startsWith("file://")
        val finalUrl = if (isAsset) "file:///android_asset/$backgroundValue" else backgroundValue

        with(ImageUtils) {
            target.context.let { ctx ->
                com.bumptech.glide.Glide.with(ctx)
                    .asBitmap()
                    .load(if (isAsset) finalUrl else android.net.Uri.parse(finalUrl))
                    .into(object : com.bumptech.glide.request.target.CustomTarget<android.graphics.Bitmap>() {
                        override fun onResourceReady(
                            resource: android.graphics.Bitmap,
                            transition: com.bumptech.glide.request.transition.Transition<in android.graphics.Bitmap>?
                        ) {
                            target.backgroundBitmap = resource
                        }

                        override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {}
                        
                        override fun onLoadFailed(errorDrawable: android.graphics.drawable.Drawable?) {
                            super.onLoadFailed(errorDrawable)
                            target.backgroundBitmap = null
                        }
                    })
            }
        }
    }

    fun colorState(color: Int): ColorStateList = ColorStateList.valueOf(color)
}
