package com.grl.sms_wa.ui.main.draws.blocked

import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseListAdapter
import com.grl.sms_wa.databinding.ItemMessageBinding

class BlockedAdapter : BaseListAdapter<BlockedContactModel, ItemMessageBinding>(
    DIFF_CALLBACK,
    ItemMessageBinding::inflate
) {
    override fun bind(binding: ItemMessageBinding, item: BlockedContactModel, position: Int) {
        binding.tvSender.text = item.displayName
        binding.tvMessage.text = item.address
        binding.tvDate.text = binding.root.context.getString(R.string.txt_blocked)
        binding.ivUnread.isVisible = false
        bindAvatar(binding, item)
    }

    private fun bindAvatar(binding: ItemMessageBinding, item: BlockedContactModel) {
        Glide.with(binding.ivAvatar).clear(binding.ivAvatar)
        binding.ivAvatar.clearColorFilter()
        binding.tvFirstName.text = ""
        binding.tvFirstName.isVisible = false

        if (item.photo != null) {
            binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivAvatar.setImageBitmap(item.photo)
            return
        }

        if (!item.hasContactName) {
            binding.ivAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.ivAvatar.setImageResource(R.drawable.ic_person)
            return
        }

        val firstLetter = item.displayName
            .trim()
            .firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

        binding.ivAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.ivAvatar.setImageResource(R.drawable.ic_person)
        binding.tvFirstName.text = firstLetter
        binding.tvFirstName.isVisible = item.hasContactName && firstLetter.isNotEmpty()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlockedContactModel>() {
            override fun areItemsTheSame(
                oldItem: BlockedContactModel,
                newItem: BlockedContactModel
            ): Boolean = oldItem.address == newItem.address

            override fun areContentsTheSame(
                oldItem: BlockedContactModel,
                newItem: BlockedContactModel
            ): Boolean = oldItem == newItem
        }
    }
}
