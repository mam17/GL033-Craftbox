package com.grl.sms_wa.ui.components.custom.activity

import android.graphics.Color
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityCustomFontBinding
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.ui.components.custom.adapter.ChatPreviewAdapter
import com.grl.sms_wa.ui.components.custom.adapter.FontAdapter
import com.grl.sms_wa.ui.components.custom.adapter.FontItem
import com.grl.sms_wa.ui.components.custom.adapter.PreviewMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomFontActivity :
    BaseActivity<ActivityCustomFontBinding>(ActivityCustomFontBinding::inflate) {
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
    private val fontAdapter by lazy { FontAdapter() }

    private var originalTheme: ThemeMessModel? = null
    private var currentTheme: ThemeMessModel? = null

    override fun initView() {
        applyStatusBarInsetToHeader()
        binding.ivBack.setOnClickListener { finish() }

        setupRecyclerView()
        setupFontRecyclerView()

        binding.ivReset.setOnClickListener {
            resetToOriginal()
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

        // Header UI
        val colMain = Color.parseColor(theme.colMain)
        binding.tvTitle.setTextColor(colMain)
        binding.tvTitle.text = getString(com.grl.sms_wa.R.string.txt_font)
        binding.ivBack.setColorFilter(colMain)
        binding.ivReset.setColorFilter(colMain)

        binding.tvFont.setTextColor(colMain)
        binding.tvFont.text = getString(com.grl.sms_wa.R.string.txt_font)

        chatAdapter.theme = currentTheme
        fontAdapter.setSelectedFont(theme.font)
    }

    private fun setupFontRecyclerView() {
        binding.rcvFont.adapter = fontAdapter
        val fonts = assets.list("fonts")?.map {
            FontItem(it.substringBeforeLast("."), it)
        } ?: emptyList()

        fontAdapter.setData(fonts)
        fontAdapter.setSelectedFont(currentTheme?.font)

        fontAdapter.setOnItemClick { item, position ->
            fontAdapter.selectItem(position)
            val fontPath = "fonts/${item.path}"
            currentTheme = currentTheme?.copy(font = fontPath)
            chatAdapter.theme = currentTheme
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

    private fun resetToOriginal() {
        val theme = originalTheme ?: return
        currentTheme = theme

        chatAdapter.theme = currentTheme
        fontAdapter.setSelectedFont(theme.font)
    }
}
