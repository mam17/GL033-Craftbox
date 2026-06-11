package com.grl.sms_wa.ui.components.custom.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemStyleBinding

data class BubbleStyleItem(val type: Int, val drawableRes: Int, var isSelected: Boolean = false)

class BubbleStyleAdapter : BaseAdapter<BubbleStyleItem, ItemStyleBinding>(ItemStyleBinding::inflate) {

    private var currentSelectedPosition = -1

    override fun bind(binding: ItemStyleBinding, item: BubbleStyleItem, position: Int) {
        binding.ivBBStyle.setImageResource(item.drawableRes)
        
        if (item.isSelected) {
            binding.flRoot.setBackgroundResource(R.drawable.bg_border_r12_fef) // Hoặc drawable viền được chọn của bạn
        } else {
            binding.flRoot.background = null
        }
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

    fun setSelectedType(type: Int) {
        val index = dataList.indexOfFirst { it.type == type }
        if (index != -1) {
            selectItem(index)
        }
    }
}
