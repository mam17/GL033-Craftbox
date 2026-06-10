package com.grl.sms_wa.ui.feature

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemFeatureBinding
import com.grl.sms_wa.domain.layer.FeatureModel

class FeatureAdapter : BaseAdapter<FeatureModel, ItemFeatureBinding>(ItemFeatureBinding::inflate) {

    var onItemSelected: ((Boolean) -> Unit)? = null

    override fun bind(
        binding: ItemFeatureBinding,
        item: FeatureModel,
        position: Int
    ) {
        binding.apply {
            tvItemFeature.text = root.context.getString(item.nameRes)
            ivFeature.setImageResource(item.iconRes)
            ivCheck.isSelected = item.selected
            tvItemFeature.isSelected = item.selected

            ivFeature.setOnClickListener {
                item.selected = !item.selected
                notifyItemChanged(position)
                onItemSelected?.invoke(item.selected)
            }
        }
    }
}