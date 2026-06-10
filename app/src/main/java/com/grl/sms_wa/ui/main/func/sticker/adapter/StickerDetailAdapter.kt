package com.grl.sms_wa.ui.main.func.sticker.adapter

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemStickerDetailBinding
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction

class StickerDetailAdapter :
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
