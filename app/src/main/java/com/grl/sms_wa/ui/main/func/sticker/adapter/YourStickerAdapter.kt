package com.grl.sms_wa.ui.main.func.sticker.adapter

import androidx.core.graphics.toColorInt
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemStickerStoreBinding
import com.grl.sms_wa.domain.layer.StickerModel
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.visible

class YourStickerAdapter :
    BaseAdapter<StickerModel, ItemStickerStoreBinding>(ItemStickerStoreBinding::inflate) {

    var onRemoveSticker: ((StickerModel) -> Unit)? = null

    override fun bind(
        binding: ItemStickerStoreBinding,
        item: StickerModel,
        position: Int
    ) {
        val spManager = SpManager.get(binding.root.context)
        val theme = spManager.getCurrentTheme()
        val mainColor = theme?.colMain?.toColorInt()
            ?: binding.root.context.getColor(R.color.col_main)

        binding.apply {
            tvNameSticker.text = item.name
            ivSticker.loadFromPathAction("file:///android_asset/${item.firstImagePath}")

            ivRemoveAdd.visible()
            ivRemoveAdd.setImageResource(R.drawable.ic_remove)
            ivRemoveAdd.setColorFilter(mainColor)

            ivRemoveAdd.setOnClickListener {
                onRemoveSticker?.invoke(item)
            }
        }
    }

}
