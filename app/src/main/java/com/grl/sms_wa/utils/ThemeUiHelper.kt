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
            return
        }

        val backgroundValue = theme.pathBG
        if (backgroundValue.startsWith("#")) {
            target.backgroundBitmap = null
            target.customSolidColor = backgroundValue.toColorInt()
            return
        }

        val bitmap = runCatching {
            target.context.assets.open(backgroundValue).use(BitmapFactory::decodeStream)
        }.getOrNull()

        target.customSolidColor = Color.TRANSPARENT
        target.backgroundBitmap = bitmap
    }

    fun colorState(color: Int): ColorStateList = ColorStateList.valueOf(color)
}
