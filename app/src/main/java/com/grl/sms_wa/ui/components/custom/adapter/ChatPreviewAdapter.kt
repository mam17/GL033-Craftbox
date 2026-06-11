package com.grl.sms_wa.ui.components.custom.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import androidx.core.graphics.toColorInt
import com.grl.sms_wa.base.adapter.BaseMultiAdapter
import com.grl.sms_wa.databinding.ItemChatDateBinding
import com.grl.sms_wa.databinding.ItemChatLeftBinding
import com.grl.sms_wa.databinding.ItemChatRightBinding
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.utils.AppEx.dpToPx
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.views.MessageBubbleView

data class PreviewMessage(
    val content: String,
    val type: Int, // 0: Date, 1: Left, 2: Right
    val time: String = ""
)

class ChatPreviewAdapter : BaseMultiAdapter<PreviewMessage>() {

    var theme: ThemeMessModel? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

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
                theme?.let { currentTheme ->
                    binding.tvDate.applyThemeFont(currentTheme.font)
                    binding.tvDate.setTextColor(currentTheme.colTextBBSent.toColorInt())
                }
            }
            is ItemChatLeftBinding -> {
                binding.tvBody.text = item.content
                binding.tvFirstName.visibility = android.view.View.GONE
                
                theme?.let { currentTheme ->
                    binding.root.applyThemeFont(currentTheme.font)
                    binding.bubbleView.setBubbleColor(currentTheme.colBGBBReceived.toColorInt())
                    applyBubbleType(
                        bubbleView = binding.bubbleView,
                        theme = currentTheme,
                        strokeColorHex = currentTheme.colStrokeBBReceived,
                        typeOneCaretPosition = 0x0080000b,
                        typeTwoCaretPosition = 0x00800053
                    )
                    binding.tvBody.setTextColor(currentTheme.colTextBBReceived.toColorInt())
                    binding.ivAvatar.setBgColor(currentTheme.colMain.toColorInt())
                }
            }
            is ItemChatRightBinding -> {
                binding.tvBody.text = item.content
                binding.tvTime.visibility = android.view.View.GONE
                binding.tvStatus.visibility = android.view.View.GONE
                binding.ivDone.visibility = android.view.View.GONE
                
                theme?.let { currentTheme ->
                    binding.root.applyThemeFont(currentTheme.font)
                    binding.bubbleView.setBubbleColor(currentTheme.colBGBBSent.toColorInt())
                    applyBubbleType(
                        bubbleView = binding.bubbleView,
                        theme = currentTheme,
                        strokeColorHex = currentTheme.colStrokeBBSent,
                        typeOneCaretPosition = 0x0080000d,
                        typeTwoCaretPosition = 0x00800055
                    )
                    binding.tvBody.setTextColor(currentTheme.colTextBBSent.toColorInt())
                }
            }
        }
    }

    private fun applyBubbleType(
        bubbleView: MessageBubbleView,
        theme: ThemeMessModel,
        strokeColorHex: String,
        typeOneCaretPosition: Int,
        typeTwoCaretPosition: Int
    ) {
        val context = bubbleView.context
        val cornerRadius = context.dpToPx(16).toFloat()
        val strokeWidth = if (theme.enableStroke && theme.widthStroke > 0) {
            theme.widthStroke.toFloat()
        } else {
            0f
        }
        val strokeColor = strokeColorHex
            .takeIf { it.isNotBlank() }
            ?.let { runCatching { "#${it.trimStart('#')}".toColorInt() }.getOrNull() }
            ?: theme.colorStroke
                .takeIf { it.isNotBlank() }
                ?.let { runCatching { "#${it.trimStart('#')}".toColorInt() }.getOrNull() }
            ?: theme.colMain.toColorInt()

        bubbleView.setStroke(
            theme.enableStroke && strokeWidth > 0f,
            strokeColor,
            strokeWidth
        )

        when (theme.typeBubble) {
            0 -> {
                bubbleView.setCaretPosition(0)
                bubbleView.setCaretSize(0, 0)
                bubbleView.setCornerRadius(cornerRadius)
            }
            1 -> {
                bubbleView.setCaretPosition(typeOneCaretPosition)
                bubbleView.setCaretSize(context.dpToPx(12), context.dpToPx(9))
                bubbleView.setCornerRadius(cornerRadius)
            }
            2 -> {
                bubbleView.setCaretPosition(typeTwoCaretPosition)
                bubbleView.setCaretSize(context.dpToPx(12), context.dpToPx(9))
                bubbleView.setCornerRadius(cornerRadius)
            }
            3 -> {
                bubbleView.setCaretPosition(typeTwoCaretPosition)
                bubbleView.setCaretSize(context.dpToPx(12), context.dpToPx(9))
                bubbleView.setCornerRadius(cornerRadius * 2)
            }
            4 -> {
                bubbleView.setCaretPosition(typeOneCaretPosition)
                bubbleView.setCaretSize(context.dpToPx(16), context.dpToPx(12))
                bubbleView.setCornerRadius(cornerRadius)
            }
            5 -> {
                bubbleView.setCaretPosition(typeTwoCaretPosition)
                bubbleView.setCaretSize(context.dpToPx(8), context.dpToPx(6))
                bubbleView.setCornerRadius(cornerRadius / 2)
            }
        }
    }
}
