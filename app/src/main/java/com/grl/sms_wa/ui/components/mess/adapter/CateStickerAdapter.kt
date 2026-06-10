package com.grl.sms_wa.ui.components.mess.adapter

import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemThemeCategoryBinding
import com.grl.sms_wa.domain.layer.ThemeMessModel

class CateStickerAdapter :
    BaseAdapter<String, ItemThemeCategoryBinding>(ItemThemeCategoryBinding::inflate) {

    private var selectedPosition = 0
    var onCateClick: ((String, Int) -> Unit)? = null
    private var theme: ThemeMessModel? = null

    fun setTheme(theme: ThemeMessModel?) {
        this.theme = theme
        notifyDataSetChanged()
    }

    fun setSelected(position: Int) {
        val old = selectedPosition
        selectedPosition = position
        notifyItemChanged(old)
        notifyItemChanged(selectedPosition)
    }


    override fun bind(
        binding: ItemThemeCategoryBinding,
        item: String,
        position: Int
    ) {
        binding.apply {
            tvCateName.text = item.uppercase()
            val isSelected = position == selectedPosition
            cvCate.isSelected = isSelected
            tvCateName.isSelected = isSelected

            theme?.let { t ->
                val mainColor = t.colMain.toColorInt()
                if (isSelected) {
                    cvCate.setCardBackgroundColor(mainColor)
                    tvCateName.setTextColor(ContextCompat.getColor(root.context, R.color.white))
                } else {
                    cvCate.setCardBackgroundColor(
                        ContextCompat.getColor(
                            root.context,
                            R.color.col_4D008CFF
                        )
                    )
                    tvCateName.setTextColor(mainColor)
                }
            }

            root.setOnClickListener {
                onCateClick?.invoke(item, position)
            }
        }

    }
}
