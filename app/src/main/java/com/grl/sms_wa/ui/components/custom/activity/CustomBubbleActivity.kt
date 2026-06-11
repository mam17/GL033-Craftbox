package com.grl.sms_wa.ui.components.custom.activity

import android.graphics.Color
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityCustomBubbleBinding
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.ui.components.custom.adapter.BubbleStyleAdapter
import com.grl.sms_wa.ui.components.custom.adapter.BubbleStyleItem
import com.grl.sms_wa.ui.components.custom.adapter.ChatPreviewAdapter
import com.grl.sms_wa.ui.components.custom.adapter.PreviewMessage
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomBubbleActivity :
    BaseActivity<ActivityCustomBubbleBinding>(ActivityCustomBubbleBinding::inflate) {
    override fun shouldApplySystemBarInsetsToRoot(): Boolean = false

    private fun applyStatusBarInsetToHeader() {
        val actionBarHeight = binding.clHeader.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(binding.clHeader) { header, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            header.layoutParams = header.layoutParams.apply {
                height = actionBarHeight + statusBarTop
            }
            header.setPadding(
                header.paddingLeft,
                statusBarTop,
                header.paddingRight,
                header.paddingBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.clHeader)
    }

    private val chatAdapter by lazy { ChatPreviewAdapter() }
    private val bubbleAdapter by lazy { BubbleStyleAdapter() }


    private var originalTheme: ThemeMessModel? = null
    private var currentTheme: ThemeMessModel? = null
    private var colBGBBReceived: String = ""
    private var colBGBBSent: String = ""
    private var colTextBBReceived: String = ""
    private var colTextBBSent: String = ""

    override fun initView() {
        applyStatusBarInsetToHeader()
        binding.ivBack.setOnClickListener { finish() }

        setupRecyclerView()
        setupBubbleRecyclerView()

        binding.btnColor.setOnClickListener { updateTab(true) }
        binding.btnStyle.setOnClickListener { updateTab(false) }

        binding.ivReset.setOnClickListener {
            resetToOriginal()
        }

        binding.llBBReceived.setOnClickListener {
            showColorPicker(getString(R.string.txt_bubble_color), colBGBBReceived) { hex ->
                colBGBBReceived = hex
                binding.ivBBColorReceivedSL.setBgColor(Color.parseColor(hex))
                updateChatPreview()
            }
        }
        binding.llBBSent.setOnClickListener {
            showColorPicker(getString(R.string.txt_bubble_color), colBGBBSent) { hex ->
                colBGBBSent = hex
                binding.ivColorBBSentSL.setBgColor(Color.parseColor(hex))
                updateChatPreview()
            }
        }
        binding.llTextColorReceived.setOnClickListener {
            showColorPicker(getString(R.string.txt_text_color), colTextBBReceived) { hex ->
                colTextBBReceived = hex
                binding.ivTextColorReceivedSL.setBgColor(Color.parseColor(hex))
                updateChatPreview()
            }
        }
        binding.llTextColorSent.setOnClickListener {
            showColorPicker(getString(R.string.txt_text_color), colTextBBSent) { hex ->
                colTextBBSent = hex
                binding.ivTextColorSentSL.setBgColor(Color.parseColor(hex))
                updateChatPreview()
            }
        }

        binding.btnApply.setOnClickListener {
            currentTheme?.let { theme ->
                spManager.saveCurrentTheme(theme)
            }
            finish()
        }
    }

    override fun initData() {
        val theme = spManager.getCurrentTheme() ?: return
        originalTheme = theme
        currentTheme = theme
        colBGBBReceived = theme.colBGBBReceived
        colBGBBSent = theme.colBGBBSent
        colTextBBReceived = theme.colTextBBReceived
        colTextBBSent = theme.colTextBBSent

        // Header UI
        val colMain = Color.parseColor(theme.colMain)
        binding.tvTitle.setTextColor(colMain)
        binding.ivBack.setColorFilter(colMain)
        binding.ivReset.setColorFilter(colMain)

        binding.ivBBReceived.setColorFilter(colMain)
        binding.ivBBSent.setColorFilter(colMain)
        binding.ivTextColorReceived.setColorFilter(colMain)
        binding.ivTextColorSent.setColorFilter(colMain)

        binding.tvTextColor.setTextColor(colMain)
        binding.tvBBColor.setTextColor(colMain)

        // Init color slots
        binding.ivBBColorReceivedSL.setBgColor(Color.parseColor(colBGBBReceived))
        binding.ivColorBBSentSL.setBgColor(Color.parseColor(colBGBBSent))
        binding.ivTextColorReceivedSL.setBgColor(Color.parseColor(colTextBBReceived))
        binding.ivTextColorSentSL.setBgColor(Color.parseColor(colTextBBSent))

        chatAdapter.theme = currentTheme
        bubbleAdapter.setSelectedType(theme.typeBubble)
        updateTab(true)
    }

    private fun setupBubbleRecyclerView() {
        binding.rcvBubble.adapter = bubbleAdapter
        val styles = listOf(
            BubbleStyleItem(0, R.drawable.bb_style0),
            BubbleStyleItem(1, R.drawable.bb_style1),
            BubbleStyleItem(2, R.drawable.bb_style2),
            BubbleStyleItem(3, R.drawable.bb_style3),
            BubbleStyleItem(4, R.drawable.bb_style4),
            BubbleStyleItem(5, R.drawable.bb_style5)
        )
        bubbleAdapter.setData(styles)
        bubbleAdapter.setOnItemClick { item, position ->
            bubbleAdapter.selectItem(position)
            currentTheme = currentTheme?.copy(typeBubble = item.type)
            updateChatPreview()
        }
    }

    private fun setupRecyclerView() {
        binding.rcvChatPreview.layoutManager = LinearLayoutManager(this)
        binding.rcvChatPreview.adapter = chatAdapter

        val fakeData = listOf(
            PreviewMessage("10:00 AM, October 10, 2025", ChatPreviewAdapter.TYPE_DATE),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_LEFT),
            PreviewMessage(
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent leo tellus",
                ChatPreviewAdapter.TYPE_RIGHT
            ),
            PreviewMessage("10:00 PM, October 20, 2025", ChatPreviewAdapter.TYPE_DATE),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_LEFT),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_RIGHT)
        )
        chatAdapter.setData(fakeData)
    }

    private fun updateTab(isColorTab: Boolean) {
        val colMain =
            runCatching { Color.parseColor(currentTheme?.colMain) }.getOrDefault(Color.BLUE)
        if (isColorTab) {
            binding.btnColor.setCardBackgroundColor(colMain)
            binding.tvAll.setTextColor(Color.WHITE)

            binding.btnStyle.setCardBackgroundColor(Color.TRANSPARENT)
            binding.tvYourSticker.setTextColor(ContextCompat.getColor(this, R.color.col_888888))

            binding.llTabColor.isVisible = true
            binding.rcvBubble.isVisible = false
        } else {
            binding.btnStyle.setCardBackgroundColor(colMain)
            binding.tvYourSticker.setTextColor(Color.WHITE)

            binding.btnColor.setCardBackgroundColor(Color.TRANSPARENT)
            binding.tvAll.setTextColor(ContextCompat.getColor(this, R.color.col_888888))

            binding.llTabColor.isVisible = false
            binding.rcvBubble.isVisible = true
        }
    }

    private fun showColorPicker(
        title: String,
        initialColorHex: String,
        onColorSelected: (String) -> Unit
    ) {
        val initialColor =
            runCatching { Color.parseColor(initialColorHex) }.getOrDefault(Color.WHITE)
        ColorPickerDialog.Builder(this, R.style.CustomColorPickerDialog)
            .setTitle(title)
            .setPositiveButton(getString(R.string.txt_ok), ColorEnvelopeListener { envelope, _ ->
                val hexColor = "#${envelope.hexCode}"
                onColorSelected(hexColor)
            })
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .show()
    }

    private fun updateChatPreview() {
        currentTheme = currentTheme?.copy(
            colBGBBReceived = colBGBBReceived,
            colBGBBSent = colBGBBSent,
            colTextBBReceived = colTextBBReceived,
            colTextBBSent = colTextBBSent
        )
        chatAdapter.theme = currentTheme
    }

    private fun resetToOriginal() {
        val theme = originalTheme ?: return
        currentTheme = theme
        colBGBBReceived = theme.colBGBBReceived
        colBGBBSent = theme.colBGBBSent
        colTextBBReceived = theme.colTextBBReceived
        colTextBBSent = theme.colTextBBSent

        // Update color slots UI
        binding.ivBBColorReceivedSL.setBgColor(Color.parseColor(colBGBBReceived))
        binding.ivColorBBSentSL.setBgColor(Color.parseColor(colBGBBSent))
        binding.ivTextColorReceivedSL.setBgColor(Color.parseColor(colTextBBReceived))
        binding.ivTextColorSentSL.setBgColor(Color.parseColor(colTextBBSent))

        // Update Adapters
        chatAdapter.theme = currentTheme
        bubbleAdapter.setSelectedType(theme.typeBubble)

        updateTab(true)
    }
}