package com.example.myapplication.ui.feature

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemFeatureBinding
import com.example.myapplication.domain.layer.FeatureModel

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