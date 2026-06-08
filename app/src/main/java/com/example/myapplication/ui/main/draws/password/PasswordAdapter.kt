package com.example.myapplication.ui.main.draws.password

import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseListAdapter
import com.example.myapplication.databinding.ItemMessageBinding

class PasswordAdapter : BaseListAdapter<PasswordContactModel, ItemMessageBinding>(
    DIFF_CALLBACK,
    ItemMessageBinding::inflate
) {
    override fun bind(binding: ItemMessageBinding, item: PasswordContactModel, position: Int) {
        binding.tvSender.text = item.displayName
        binding.tvMessage.text = item.address
        binding.tvDate.text = binding.root.context.getString(R.string.txt_set_password)
        binding.ivUnread.isVisible = false
        bindAvatar(binding, item)
    }

    private fun bindAvatar(binding: ItemMessageBinding, item: PasswordContactModel) {
        Glide.with(binding.ivAvatar).clear(binding.ivAvatar)
        binding.ivAvatar.clearColorFilter()

        if (item.photo != null) {
            binding.tvFirstName.isVisible = false
            binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            binding.ivAvatar.setImageBitmap(item.photo)
            return
        }

        if (!item.hasContactName) {
            binding.ivAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
            binding.ivAvatar.setImageResource(R.drawable.ic_person)
            binding.tvFirstName.isVisible = false
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
        binding.tvFirstName.isVisible = firstLetter.isNotEmpty()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<PasswordContactModel>() {
            override fun areItemsTheSame(
                oldItem: PasswordContactModel,
                newItem: PasswordContactModel
            ): Boolean = oldItem.address == newItem.address

            override fun areContentsTheSame(
                oldItem: PasswordContactModel,
                newItem: PasswordContactModel
            ): Boolean = oldItem == newItem
        }
    }
}
