package com.example.myapplication.ui.components.mess.adapter

import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseListAdapter
import com.example.myapplication.databinding.ItemMediaMessBinding
import com.example.myapplication.utils.AppEx.isImageUri
import com.example.myapplication.utils.ImageUtils

data class MessengerMediaItem(
    val path: String,
    val senderName: String,
    val date: Long
)

class MessengerMediaAdapter : BaseListAdapter<MessengerMediaItem, ItemMediaMessBinding>(
    DIFF_CALLBACK,
    ItemMediaMessBinding::inflate
) {
    override fun bind(binding: ItemMediaMessBinding, item: MessengerMediaItem, position: Int) {
        val radius = binding.root.resources.getDimensionPixelSize(R.dimen.size10)
        binding.ivPlay.isVisible = !item.path.isImageUri(binding.root.context)
        with(ImageUtils) {
            binding.ivMedia.loadFromPathAction(
                path = item.path,
                radius = radius,
                isCenterCrop = true
            )
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessengerMediaItem>() {
            override fun areItemsTheSame(
                oldItem: MessengerMediaItem,
                newItem: MessengerMediaItem
            ): Boolean {
                return oldItem.path == newItem.path && oldItem.date == newItem.date
            }

            override fun areContentsTheSame(
                oldItem: MessengerMediaItem,
                newItem: MessengerMediaItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }
}
