package com.example.myapplication.utils

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.widget.TextViewCompat
import com.example.myapplication.utils.SpManager
import java.util.concurrent.ConcurrentHashMap

object ViewEx {
    private val typefaceCache = ConcurrentHashMap<String, Typeface>()

    fun ImageView.tintColor(color: Int) {
        this.setColorFilter(color, PorterDuff.Mode.SRC_IN)
    }

    fun ImageView.tintColorRes(colorRes: Int) {
        this.setColorFilter(ContextCompat.getColor(this.context, colorRes), PorterDuff.Mode.SRC_IN)
    }

    fun TextView.textColorRes(colorRes: Int) {
        this.setTextColor(ContextCompat.getColor(this.context, colorRes))
    }

    fun View.gone() {
        visibility = View.GONE
    }

    fun View.invisible() {
        visibility = View.INVISIBLE
    }

    fun View.visible() {
        visibility = View.VISIBLE
    }

    fun TextView.setTint(color: Int) {
        this.setTextColor(color)
        TextViewCompat.setCompoundDrawableTintList(
            this,
            ColorStateList.valueOf(color)
        )
    }

    fun View.applyThemeFont(fontPath: String?) {
        if (fontPath.isNullOrBlank()) return

        if (this is TextView) {
            try {
                val typeface = typefaceCache.getOrPut(fontPath) {
                    Typeface.createFromAsset(this.context.assets, fontPath)
                }
                this.typeface = typeface
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).applyThemeFont(fontPath)
            }
        }
    }

    fun View.applyCurrentThemeFont() {
        applyThemeFont(SpManager.get(context).getCurrentTheme()?.font)
    }

    fun View.applyThemeTextColor(colorHex: String?) {
        if (colorHex.isNullOrBlank()) return

        val colorInt = try {
            colorHex.toColorInt()
        } catch (_: IllegalArgumentException) {
            return
        }

        applyThemeTextColor(colorInt)
    }

    fun View.applyThemeTextColor(colorInt: Int) {
        if (this is TextView && this !is EditText) {
            setTextColor(colorInt)
        }
        if (this is ViewGroup) {
            for (i in 0 until childCount) {
                getChildAt(i).applyThemeTextColor(colorInt)
            }
        }
    }
}
