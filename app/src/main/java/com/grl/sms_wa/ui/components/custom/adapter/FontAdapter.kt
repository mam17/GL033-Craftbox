package com.grl.sms_wa.ui.components.custom.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemFontBinding
import com.grl.sms_wa.utils.ViewEx.applyThemeFont

data class FontItem(val name: String, val path: String, var isSelected: Boolean = false)

class FontAdapter : BaseAdapter<FontItem, ItemFontBinding>(ItemFontBinding::inflate) {

    private var currentSelectedPosition = -1

    override fun bind(binding: ItemFontBinding, item: FontItem, position: Int) {
        binding.tvFont.text = item.name
        binding.tvFont.applyThemeFont("fonts/${item.path}")
        binding.ivCheck.isSelected = item.isSelected
    }

    override fun selectItem(position: Int) {
        if (currentSelectedPosition != -1 && currentSelectedPosition < dataList.size) {
            dataList[currentSelectedPosition].isSelected = false
            notifyItemChanged(currentSelectedPosition)
        }
        currentSelectedPosition = position
        dataList[position].isSelected = true
        notifyItemChanged(position)
    }

    fun setSelectedFont(fontPath: String?) {
        val path = fontPath?.substringAfter("fonts/") ?: fontPath
        val index = dataList.indexOfFirst { it.path == path }
        if (index != -1) {
            selectItem(index)
        }
    }
}
