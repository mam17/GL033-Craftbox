package com.example.myapplication.ui.components.custom.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.myapplication.base.adapter.BaseMultiAdapter
import com.example.myapplication.databinding.ItemChatDateBinding
import com.example.myapplication.databinding.ItemChatLeftBinding
import com.example.myapplication.databinding.ItemChatRightBinding

data class PreviewMessage(
    val content: String,
    val type: Int, // 0: Date, 1: Left, 2: Right
    val time: String = ""
)

class ChatPreviewAdapter : BaseMultiAdapter<PreviewMessage>() {

    companion object {
        const val TYPE_DATE = 0
        const val TYPE_LEFT = 1
        const val TYPE_RIGHT = 2
    }

    override fun getViewType(item: PreviewMessage, position: Int): Int = item.type

    override fun getBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding {
        return when (viewType) {
            TYPE_DATE -> ItemChatDateBinding.inflate(inflater, parent, false)
            TYPE_LEFT -> ItemChatLeftBinding.inflate(inflater, parent, false)
            TYPE_RIGHT -> ItemChatRightBinding.inflate(inflater, parent, false)
            else -> ItemChatLeftBinding.inflate(inflater, parent, false)
        }
    }

    override fun bind(binding: ViewBinding, item: PreviewMessage, position: Int) {
        when (binding) {
            is ItemChatDateBinding -> {
                binding.tvDate.text = item.content
            }
            is ItemChatLeftBinding -> {
                binding.tvBody.text = item.content
                // Hidden avatar text or something if needed
                binding.tvFirstName.visibility = android.view.View.GONE
            }
            is ItemChatRightBinding -> {
                binding.tvBody.text = item.content
            }
        }
    }
}
