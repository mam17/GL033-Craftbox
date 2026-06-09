package com.example.myapplication.ui.main.func.theme.adapter

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemThemeCategoryBinding
import com.example.myapplication.domain.layer.CategoryThemeModel
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.ViewEx.applyThemeFont

class CategoryThemeAdapter :
    BaseAdapter<CategoryThemeModel, ItemThemeCategoryBinding>(ItemThemeCategoryBinding::inflate) {

    private var selectedPosition = 0
    private var mThemeModel: ThemeMessModel? = null

    fun setTheme(theme: ThemeMessModel?) {
        this.mThemeModel = theme
        notifyDataSetChanged()
    }

    fun setSelectedPosition(position: Int) {
        if (dataList.isEmpty()) return
        val oldPosition = selectedPosition
        selectedPosition = position.coerceIn(dataList.indices)
        if (oldPosition in dataList.indices) {
            notifyItemChanged(oldPosition)
        }
        if (selectedPosition in dataList.indices) {
            notifyItemChanged(selectedPosition)
        }
    }

    override fun bind(
        binding: ItemThemeCategoryBinding,
        item: CategoryThemeModel,
        position: Int
    ) {
        binding.apply {
            val context = root.context
            tvCateName.text = item.nameCategory

            val isItemSelected = position == selectedPosition
            tvCateName.isSelected = isItemSelected

            mThemeModel?.let { theme ->
                root.applyThemeFont(theme.font)
                val bgColor =
                    if (isItemSelected) theme.colBGBBSent.toColorInt() else theme.colBGBBReceived.toColorInt()
                cvCate.setCardBackgroundColor(bgColor)

                val textColor = if (isItemSelected) Color.WHITE else theme.colBGBBSent.toColorInt()
                tvCateName.setTextColor(textColor)
            } ?: run {
                val bgColorRes = if (isItemSelected) R.color.col_main else R.color.col_4D008CFF
                cvCate.setCardBackgroundColor(ContextCompat.getColor(context, bgColorRes))
                tvCateName.setTextColor(ContextCompat.getColorStateList(context, R.color.sl_cate))
            }

        }
    }
}
