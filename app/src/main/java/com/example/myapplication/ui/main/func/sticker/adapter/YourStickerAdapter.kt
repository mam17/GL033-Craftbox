package com.example.myapplication.ui.main.func.sticker.adapter

import android.view.View
import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemStickerStoreBinding
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.utils.ImageUtils.loadFromPathAction
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.visible

class YourStickerAdapter :
    BaseAdapter<StickerModel, ItemStickerStoreBinding>(ItemStickerStoreBinding::inflate) {


    var onToggleSticker: ((StickerModel) -> Unit)? = null
    var isEditMode: Boolean = false
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
//            ivRemoveAdd.setColorFilter(mainColor)

            if (item.isAdded) {
                if (isEditMode) {
                    ivRemoveAdd.setImageResource(R.drawable.ic_remove)
                } else {
                    ivRemoveAdd.setImageResource(R.drawable.ic_check)
                }
            } else {
                ivRemoveAdd.setImageResource(R.drawable.ic_add_sticker)
            }

            ivRemoveAdd.setOnClickListener {
                onToggleSticker?.invoke(item)
            }
        }
    }

}
