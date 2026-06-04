package com.example.myapplication.ui.uninstall

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemLanguageBinding
import com.example.myapplication.utils.ViewEx.gone

class AskUninstallAdapter : BaseAdapter<String, ItemLanguageBinding>(ItemLanguageBinding::inflate) {
    private var posSelected = -1
    override fun bind(
        binding: ItemLanguageBinding,
        item: String,
        position: Int
    ) {
        binding.apply {
            imgLanguage.gone()
            tvTitleLanguage.text = item
            swLanguage.isSelected = posSelected == position
        }
    }

}