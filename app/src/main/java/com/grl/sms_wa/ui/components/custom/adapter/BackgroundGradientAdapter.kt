package com.grl.sms_wa.ui.components.custom.adapter

import androidx.core.content.ContextCompat
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemColorGradientBinding
import com.grl.sms_wa.ui.components.custom.viewmodel.BackgroundGradient
import com.grl.sms_wa.ui.components.custom.viewmodel.GradientDirection
import com.grl.sms_wa.views.CustomBackgroundView

class BackgroundGradientAdapter :
    BaseAdapter<BackgroundGradient, ItemColorGradientBinding>(ItemColorGradientBinding::inflate) {

    private var selectedPosition = NO_POSITION
    private var displayDirection = GradientDirection.HORIZONTAL

    override fun bind(
        binding: ItemColorGradientBinding,
        item: BackgroundGradient,
        position: Int
    ) {
        binding.itemColorGradient.gradientColors = item.colors.toIntArray()
        binding.itemColorGradient.gradientOrientation = when (displayDirection) {
            GradientDirection.HORIZONTAL -> CustomBackgroundView.GradientOrientation.LEFT_RIGHT
            GradientDirection.VERTICAL -> CustomBackgroundView.GradientOrientation.TOP_BOTTOM
        }
        binding.itemColorGradient.setStroke(
            enabled = position == selectedPosition,
            color = ContextCompat.getColor(binding.root.context, R.color.col_main),
            widthPx = binding.root.resources.getDimension(R.dimen.size1)
        )
    }

    override fun selectItem(position: Int) {
        if (position !in getData().indices || position == selectedPosition) return
        val previousPosition = selectedPosition
        selectedPosition = position
        if (previousPosition != NO_POSITION) notifyItemChanged(previousPosition)
        notifyItemChanged(selectedPosition)
    }

    fun clearSelection() {
        if (selectedPosition == NO_POSITION) return
        val previousPosition = selectedPosition
        selectedPosition = NO_POSITION
        notifyItemChanged(previousPosition)
    }

    fun setDirection(direction: GradientDirection) {
        if (displayDirection == direction) return
        displayDirection = direction
        notifyItemRangeChanged(0, itemCount)
    }

    private companion object {
        const val NO_POSITION = -1
    }
}
