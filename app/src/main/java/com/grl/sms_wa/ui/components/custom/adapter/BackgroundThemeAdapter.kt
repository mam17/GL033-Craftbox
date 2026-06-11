package com.grl.sms_wa.ui.components.custom.adapter

import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemBackgroundThemeBinding
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction

sealed interface BackgroundThemeItem {
    data object Library : BackgroundThemeItem
    data class Asset(val path: String) : BackgroundThemeItem
}

class BackgroundThemeAdapter :
    BaseAdapter<BackgroundThemeItem, ItemBackgroundThemeBinding>(ItemBackgroundThemeBinding::inflate) {

    override fun bind(
        binding: ItemBackgroundThemeBinding,
        item: BackgroundThemeItem,
        position: Int
    ) {
        val isLibrary = item is BackgroundThemeItem.Library
        binding.llLibrary.isVisible = isLibrary
        if (item is BackgroundThemeItem.Asset) {
            binding.ivBGTheme.loadFromPathAction("file:///android_asset/${item.path}")
        } else {
            Glide.with(binding.ivBGTheme).clear(binding.ivBGTheme)
            binding.ivBGTheme.setImageDrawable(null)
        }
    }
}
