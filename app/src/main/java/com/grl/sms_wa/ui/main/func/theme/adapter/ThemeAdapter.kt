package com.grl.sms_wa.ui.main.func.theme.adapter

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemThemeBinding
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction
import com.grl.sms_wa.utils.asImageSource

class ThemeAdapter : BaseAdapter<ThemeMessModel, ItemThemeBinding>(ItemThemeBinding::inflate) {

    override fun bind(
        binding: ItemThemeBinding,
        item: ThemeMessModel,
        position: Int
    ) {
        binding.apply {
            val pathImage = item.pathThemePreview.asImageSource()
            ivThemePreview.loadFromPathAction(pathImage)
        }
    }
}
