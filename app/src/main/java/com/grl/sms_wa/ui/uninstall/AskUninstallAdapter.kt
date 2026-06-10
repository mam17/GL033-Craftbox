package com.grl.sms_wa.ui.uninstall

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemLanguageBinding
import com.grl.sms_wa.utils.ViewEx.gone

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