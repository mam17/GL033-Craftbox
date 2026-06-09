package com.example.myapplication.ui.main.func.sticker.adapter

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemStickerPackBinding
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.utils.ImageUtils.loadFromPathAction

class StickerAdapter :
    BaseAdapter<StickerModel, ItemStickerPackBinding>(ItemStickerPackBinding::inflate) {

    override fun bind(
        binding: ItemStickerPackBinding,
        item: StickerModel,
        position: Int
    ) {
        binding.apply {
            ivStickerLogo.loadFromPathAction("file:///android_asset/${item.previewPath}")
        }
    }
}
