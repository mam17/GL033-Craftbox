package com.example.myapplication.ui.components.mess.adapter

import android.provider.Telephony
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseMultiListAdapter
import com.example.myapplication.data.model.SmsMessage
import com.example.myapplication.databinding.ItemChatDateBinding
import com.example.myapplication.databinding.ItemChatLeftBinding
import com.example.myapplication.databinding.ItemChatRightBinding
import com.example.myapplication.ui.components.mess.MessageItem
import com.example.myapplication.utils.AppEx.formatToOrdinalDate
import com.example.myapplication.utils.ImageUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MessengerChatAdapter(
    private val senderContactName: String?,
    private val senderPhotoUri: String?
) : BaseMultiListAdapter<MessageItem>(DIFF_CALLBACK) {

    private var onMediaClick: ((SmsMessage) -> Unit)? = null

    override fun getBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding {
        return when (viewType) {
            VIEW_TYPE_DATE -> ItemChatDateBinding.inflate(inflater, parent, false)
            VIEW_TYPE_RIGHT -> ItemChatRightBinding.inflate(inflater, parent, false)
            else -> ItemChatLeftBinding.inflate(inflater, parent, false)
        }
    }

    override fun bind(binding: ViewBinding, item: MessageItem, position: Int) {
        when (binding) {
            is ItemChatDateBinding -> {
                if (item is MessageItem.DateHeader) {
                    binding.tvDate.text = item.date.formatToOrdinalDate()
                }
            }
            is ItemChatRightBinding -> {
                if (item is MessageItem.MessageContent) {
                    binding.tvBody.text = item.message.body
                    binding.tvTime.text = item.message.date.formatMessageTime()
                    val isFailed = item.message.type == Telephony.Sms.MESSAGE_TYPE_FAILED
                    binding.tvStatus.isVisible = isFailed
                    binding.ivDone.isVisible = !isFailed
                    bindMedia(binding, item.message)
                }
            }
            is ItemChatLeftBinding -> {
                if (item is MessageItem.MessageContent) {
                    binding.tvBody.text = item.message.body
                    binding.tvBody.setTextColor(
                        ContextCompat.getColor(binding.root.context, R.color.black)
                    )
                    bindSenderAvatar(binding)
                    bindMedia(binding, item.message)
                }
            }
        }
    }

    override fun getViewType(item: MessageItem, position: Int): Int {
        return when (item) {
            is MessageItem.DateHeader -> VIEW_TYPE_DATE
            is MessageItem.MessageContent -> {
                if (item.message.isOutgoing()) {
                    VIEW_TYPE_RIGHT
                } else {
                    VIEW_TYPE_LEFT
                }
            }
        }
    }

    private fun bindSenderAvatar(binding: ItemChatLeftBinding) {
        val firstNameLetter = senderContactName
            ?.trim()
            ?.takeIf { it.hasLetters() }
            ?.firstOrNull()
            ?.uppercaseChar()
            ?.toString()
            .orEmpty()

        Glide.with(binding.ivAvatar).clear(binding.ivAvatar)
        binding.ivAvatar.clearColorFilter()

        if (!senderPhotoUri.isNullOrBlank()) {
            binding.tvFirstName.isVisible = false
            binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(binding.ivAvatar)
                .load(senderPhotoUri)
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

    private fun bindMedia(binding: ItemChatLeftBinding, message: SmsMessage) {
        val mediaUri = message.mediaUri
        Glide.with(binding.ivImage).clear(binding.ivImage)
        binding.ivPlayVideo.isVisible = false

        if (mediaUri.isNullOrBlank()) {
            binding.flMedia.isVisible = false
            binding.flMedia.setOnClickListener(null)
            binding.ivImage.setImageDrawable(null)
            return
        }

        binding.flMedia.isVisible = true
        binding.flMedia.setOnClickListener {
            onMediaClick?.invoke(message)
        }
        val radius = binding.root.resources.getDimensionPixelSize(R.dimen.size8)
        with(ImageUtils) {
            binding.ivImage.loadFromPathAction(
                path = mediaUri,
                radius = radius,
                isCenterCrop = false
            )
        }
    }

    private fun bindMedia(binding: ItemChatRightBinding, message: SmsMessage) {
        val mediaUri = message.mediaUri
        binding.tvBody.setTextColor(ContextCompat.getColor(binding.root.context, R.color.white))
        binding.tvTime.setTextColor(ContextCompat.getColor(binding.root.context, R.color.grey))
        binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, R.color.red))
        Glide.with(binding.ivImage).clear(binding.ivImage)
        binding.ivPlayVideo.isVisible = false

        if (mediaUri.isNullOrBlank()) {
            binding.flMedia.isVisible = false
            binding.flMedia.setOnClickListener(null)
            binding.ivImage.setImageDrawable(null)
            return
        }

        binding.flMedia.isVisible = true
        binding.flMedia.setOnClickListener {
            onMediaClick?.invoke(message)
        }
        val radius = binding.root.resources.getDimensionPixelSize(R.dimen.size8)
        with(ImageUtils) {
            binding.ivImage.loadFromPathAction(
                path = mediaUri,
                radius = radius,
                isCenterCrop = false
            )
        }
    }

    private fun String.hasLetters(): Boolean {
        return any { it.isLetter() }
    }

    private fun SmsMessage.isOutgoing(): Boolean {
        return type == Telephony.Sms.MESSAGE_TYPE_SENT ||
            type == Telephony.Sms.MESSAGE_TYPE_OUTBOX ||
            type == Telephony.Sms.MESSAGE_TYPE_QUEUED
    }

    private fun Long.formatMessageTime(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(this))
    }

    fun setOnMediaClick(callback: (SmsMessage) -> Unit) {
        onMediaClick = callback
    }

    companion object {
        private const val VIEW_TYPE_DATE = 0
        private const val VIEW_TYPE_LEFT = 1
        private const val VIEW_TYPE_RIGHT = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageItem>() {
            override fun areItemsTheSame(
                oldItem: MessageItem,
                newItem: MessageItem
            ): Boolean {
                return when {
                    oldItem is MessageItem.DateHeader && newItem is MessageItem.DateHeader -> {
                        oldItem.date == newItem.date
                    }
                    oldItem is MessageItem.MessageContent && newItem is MessageItem.MessageContent -> {
                        oldItem.message.id == newItem.message.id
                    }
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: MessageItem,
                newItem: MessageItem
            ): Boolean = oldItem == newItem
        }
    }
}
