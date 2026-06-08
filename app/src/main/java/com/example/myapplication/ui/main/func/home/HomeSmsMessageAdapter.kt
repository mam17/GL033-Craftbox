package com.example.myapplication.ui.main.func.home

import android.text.format.DateFormat
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseListAdapter
import com.example.myapplication.databinding.ItemMessageBinding
import com.example.myapplication.sms_helper.SmsMessageModel
import java.util.Date

class HomeSmsMessageAdapter : BaseListAdapter<SmsMessageModel, ItemMessageBinding>(
    DIFF_CALLBACK,
    ItemMessageBinding::inflate
) {
    override fun bind(binding: ItemMessageBinding, item: SmsMessageModel, position: Int) {
        val context = binding.root.context
        val sender = item.contactName
            ?: item.address.ifBlank { context.getString(R.string.txt_unknown_sender) }

        bindAvatar(binding, item)
        binding.tvSender.text = sender
        binding.tvSender.setTextColor(ContextCompat.getColor(context, R.color.col_main))
        binding.tvMessage.text = item.body
        binding.tvMessage.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.tvDate.text = DateFormat.format(DATE_FORMAT, Date(item.dateMillis))
        binding.tvDate.setTextColor(ContextCompat.getColor(context, R.color.gray))
        binding.ivUnread.isVisible = !item.isRead
    }

    private fun bindAvatar(binding: ItemMessageBinding, item: SmsMessageModel) {
        val photoUri = item.contactPhotoUri
        val firstNameLetter = item.contactName
            ?.trim()
            ?.firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

        Glide.with(binding.ivAvatar).clear(binding.ivAvatar)
        binding.ivAvatar.clearColorFilter()

        if (!photoUri.isNullOrBlank()) {
            binding.tvFirstName.isVisible = false
            binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(binding.ivAvatar)
                .load(photoUri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivAvatar)
            return
        }

        binding.ivAvatar.scaleType = ImageView.ScaleType.FIT_CENTER
        binding.ivAvatar.setImageResource(R.drawable.ic_person)
        binding.tvFirstName.text = firstNameLetter
        binding.tvFirstName.isVisible = firstNameLetter.isNotEmpty()
    }

    companion object {
        private const val DATE_FORMAT = "MMM d, yyyy"

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SmsMessageModel>() {
            override fun areItemsTheSame(
                oldItem: SmsMessageModel,
                newItem: SmsMessageModel
            ): Boolean = oldItem.threadId == newItem.threadId

            override fun areContentsTheSame(
                oldItem: SmsMessageModel,
                newItem: SmsMessageModel
            ): Boolean = oldItem == newItem
        }
    }
}
