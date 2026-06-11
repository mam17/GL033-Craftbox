package com.grl.sms_wa.ui.language

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemLanguageBinding
import com.grl.sms_wa.domain.layer.LanguageModel

class LanguageAdapter :
    BaseAdapter<LanguageModel, ItemLanguageBinding>(ItemLanguageBinding::inflate) {

    override fun bind(binding: ItemLanguageBinding, item: LanguageModel, position: Int) {
        binding.imgLanguage.setImageResource(item.iconRes)
        binding.tvTitleLanguage.setText(item.nameRes)
        binding.swLanguage.isSelected = item.selected

        binding.root.isSelected = item.selected
    }

    override fun selectItem(position: Int) {
        if (position !in dataList.indices) return

        dataList.forEachIndexed { index, model ->
            if (model.selected) {
                model.selected = false
                notifyItemChanged(index)
            }
        }

        dataList[position].selected = true
        notifyItemChanged(position)
    }

    fun getSelectedModel(): LanguageModel? {
        return dataList.find { it.selected }
    }
}