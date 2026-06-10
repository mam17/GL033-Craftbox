package com.grl.sms_wa.ui.components.custom.activity

import android.graphics.BitmapFactory
import android.graphics.Color
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityCustomThemeBgBinding
import com.grl.sms_wa.ui.components.custom.adapter.ChatPreviewAdapter
import com.grl.sms_wa.ui.components.custom.adapter.PreviewMessage
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomBGThemeActivity :
    BaseActivity<ActivityCustomThemeBgBinding>(ActivityCustomThemeBgBinding::inflate) {

    private val chatAdapter by lazy { ChatPreviewAdapter() }
    private var currentOverlayAlpha = 0

    override fun initView() {
        setupRecyclerView()
        setupControls()

    }

    private fun setupRecyclerView() {
        binding.rcvChatPreview.layoutManager = LinearLayoutManager(this@CustomBGThemeActivity)
        binding.rcvChatPreview.adapter = chatAdapter

        val fakeData = listOf(
            PreviewMessage("10:00 AM, October 10, 2025", ChatPreviewAdapter.TYPE_DATE),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_LEFT),
            PreviewMessage("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Praesent leo tellus", ChatPreviewAdapter.TYPE_RIGHT),
            PreviewMessage("10:00 PM, October 20, 2025", ChatPreviewAdapter.TYPE_DATE),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_LEFT),
            PreviewMessage("Lorem ipsum dolor sit amet", ChatPreviewAdapter.TYPE_RIGHT)
        )
        chatAdapter.setData(fakeData)
    }

    private fun setupControls() {
        binding.ivBack.setOnClickListener { onBack() }
        
        binding.ivReset.setOnClickListener {
            binding.sbBlur.progress = 0
            binding.sbOverlay.progress = 0
            binding.backgroundTheme.blurEnabled = false
            binding.backgroundTheme.overlayColor = Color.TRANSPARENT
        }

        binding.sbBlur.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress > 0) {
                    binding.backgroundTheme.blurEnabled = true
                    binding.backgroundTheme.blurRadius = progress.toFloat().coerceIn(1f, 25f)
                } else {
                    binding.backgroundTheme.blurEnabled = false
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.sbOverlay.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentOverlayAlpha = (progress * 2.55).toInt() // 0-100 to 0-255
                binding.backgroundTheme.overlayColor = Color.argb(currentOverlayAlpha, 0, 0, 0)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnPicture.setOnClickListener {
            updateTabSelection(0)
        }
        binding.btnColor.setOnClickListener {
            updateTabSelection(1)
        }
        binding.btnGradient.setOnClickListener {
            updateTabSelection(2)
        }

        binding.btnApply.setOnClickListener {
            finish()
        }
    }

    private fun updateTabSelection(index: Int) {
        binding.btnPicture.setBackgroundResource(if (index == 0) R.drawable.bg_tab_selected else R.drawable.bg_white_r8)
        binding.btnColor.setBackgroundResource(if (index == 1) R.drawable.bg_tab_selected else R.drawable.bg_white_r8)
        binding.btnGradient.setBackgroundResource(if (index == 2) R.drawable.bg_tab_selected else R.drawable.bg_white_r8)
    }

    override fun initData() = Unit
}
