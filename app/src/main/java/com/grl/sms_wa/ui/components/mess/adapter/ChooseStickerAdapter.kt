package com.grl.sms_wa.ui.components.mess.adapter

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemStickerDetailBinding
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction
import com.grl.sms_wa.utils.asImageSource

class ChooseStickerAdapter :
    BaseAdapter<String, ItemStickerDetailBinding>(ItemStickerDetailBinding::inflate) {

    override fun bind(
        binding: ItemStickerDetailBinding,
        item: String,
        position: Int
    ) {
        binding.ivStickerDT.loadFromPathAction(
            item.asImageSource(),
            isCenterCrop = false
        )
    }
}
