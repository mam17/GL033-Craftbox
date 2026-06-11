package com.grl.sms_wa.ui.components.custom.adapter

import androidx.core.content.ContextCompat
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemColorBinding

class BackgroundColorAdapter :
    BaseAdapter<Int, ItemColorBinding>(ItemColorBinding::inflate) {

    private var selectedPosition = NO_POSITION

    override fun bind(binding: ItemColorBinding, item: Int, position: Int) {
        binding.ivColor.setBgColor(item)
        binding.ivColor.setStroke(
            enable = position == selectedPosition,
            color = ContextCompat.getColor(binding.root.context, R.color.col_main),
            width = binding.root.resources.getDimension(R.dimen.size1)
        )
    }

    override fun selectItem(position: Int) {
        if (position !in getData().indices || position == selectedPosition) return
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition != NO_POSITION) notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    fun clearSelection() {
        if (selectedPosition == NO_POSITION) return
        val previousPosition = selectedPosition
        selectedPosition = NO_POSITION
        notifyItemChanged(previousPosition)
    }

    private companion object {
        const val NO_POSITION = -1
    }
}
