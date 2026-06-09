package com.example.myapplication.ui.main.func.theme.adapter

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemThemeBinding
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.ImageUtils.loadFromPathAction

class ThemeAdapter : BaseAdapter<ThemeMessModel, ItemThemeBinding>(ItemThemeBinding::inflate) {

    override fun bind(
        binding: ItemThemeBinding,
        item: ThemeMessModel,
        position: Int
    ) {
        binding.apply {
            val pathImage = "file:///android_asset/${item.pathThemePreview}"
            ivThemePreview.loadFromPathAction(pathImage)
        }
    }
}
