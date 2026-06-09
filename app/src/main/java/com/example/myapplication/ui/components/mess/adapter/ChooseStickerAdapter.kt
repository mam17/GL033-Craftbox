package com.example.myapplication.ui.components.mess.adapter

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemStickerDetailBinding
import com.example.myapplication.utils.ImageUtils.loadFromPathAction

class ChooseStickerAdapter :
    BaseAdapter<String, ItemStickerDetailBinding>(ItemStickerDetailBinding::inflate) {

    override fun bind(
        binding: ItemStickerDetailBinding,
        item: String,
        position: Int
    ) {
        binding.ivStickerDT.loadFromPathAction(
            "file:///android_asset/$item",
            isCenterCrop = false
        )
    }
}
