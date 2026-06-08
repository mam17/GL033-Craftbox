package com.example.myapplication.utils

import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat

object ViewEx {
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

    fun View.applyThemeFont(fontPath: String) {
        if (this is TextView) {
            try {
                val typeface = Typeface.createFromAsset(this.context.assets, fontPath)
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
}