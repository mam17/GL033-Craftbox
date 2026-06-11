package com.grl.sms_wa.ui.main.func.sticker.adapter

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemStickerPackBinding
import com.grl.sms_wa.domain.layer.StickerModel
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction
import com.grl.sms_wa.utils.asImageSource

class StickerAdapter :
    BaseAdapter<StickerModel, ItemStickerPackBinding>(ItemStickerPackBinding::inflate) {

    override fun bind(
        binding: ItemStickerPackBinding,
        item: StickerModel,
        position: Int
    ) {
        binding.apply {
            ivStickerLogo.loadFromPathAction(item.previewPath.asImageSource())
        }
    }
}
