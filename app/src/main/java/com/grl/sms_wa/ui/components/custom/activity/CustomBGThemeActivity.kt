package com.grl.sms_wa.ui.components.custom.activity

import android.graphics.Color
import android.widget.SeekBar
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityCustomThemeBgBinding
import com.grl.sms_wa.ui.components.custom.adapter.ChatPreviewAdapter
import com.grl.sms_wa.ui.components.custom.adapter.BackgroundColorAdapter
import com.grl.sms_wa.ui.components.custom.adapter.BackgroundGradientAdapter
import com.grl.sms_wa.ui.components.custom.adapter.PreviewMessage
import com.grl.sms_wa.ui.components.custom.fragment.PictureBGFragment
import com.grl.sms_wa.ui.components.custom.viewmodel.CustomBackgroundSelection
import com.grl.sms_wa.ui.components.custom.viewmodel.CustomBGViewModel
import com.grl.sms_wa.ui.components.custom.viewmodel.GradientDirection
import com.grl.sms_wa.utils.ImageUtils
import com.grl.sms_wa.views.CustomBackgroundView
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CustomBGThemeActivity :
    BaseActivity<ActivityCustomThemeBgBinding>(ActivityCustomThemeBgBinding::inflate) {

    private val viewModel: CustomBGViewModel by viewModels()
    private val chatAdapter by lazy { ChatPreviewAdapter() }
    private val colorAdapter by lazy { BackgroundColorAdapter() }
    private val gradientAdapter by lazy { BackgroundGradientAdapter() }
    private var currentOverlayAlpha = 0

    override fun initView() {
        applyStatusBarInsetToHeader()
        setupRecyclerView()
        setupColorOptions()
        setupGradientOptions()
        setupControls()
    }

    override fun shouldApplySystemBarInsetsToRoot(): Boolean = false

    private fun applyStatusBarInsetToHeader() {
        val actionBarHeight = binding.clHeader.layoutParams.height
        ViewCompat.setOnApplyWindowInsetsListener(binding.clHeader) { header, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            header.updateLayoutParams {
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

    private fun setupRecyclerView() {
        binding.rcvChatPreview.layoutManager = LinearLayoutManager(this@CustomBGThemeActivity)
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

    private fun setupColorOptions() {
        binding.layoutColor.rcvColor.apply {
            layoutManager = LinearLayoutManager(
                this@CustomBGThemeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = colorAdapter
        }
        colorAdapter.setData(viewModel.presetColors)
        colorAdapter.setOnItemClick { color, position ->
            colorAdapter.selectItem(position)
            viewModel.selectSolidColor(color)
        }
        binding.layoutColor.ivFilter.setOnClickListener {
            showColorPicker()
        }
    }

    private fun showColorPicker() {
        ColorPickerDialog.Builder(this, R.style.CustomColorPickerDialog)
            .setTitle(getString(R.string.txt_color))
            .setPositiveButton(
                getString(R.string.txt_ok),
                ColorEnvelopeListener { envelope: ColorEnvelope, _: Boolean ->
                    colorAdapter.clearSelection()
                    viewModel.selectSolidColor(envelope.color)
                }
            )
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .show()
    }

    private fun setupGradientOptions() {
        binding.layoutColorGradient.rcvColor.apply {
            layoutManager = LinearLayoutManager(
                this@CustomBGThemeActivity,
                LinearLayoutManager.HORIZONTAL,
                false
            )
            adapter = gradientAdapter
        }
        gradientAdapter.setData(viewModel.presetGradients)
        gradientAdapter.setOnItemClick { gradient, position ->
            gradientAdapter.selectItem(position)
            viewModel.selectGradient(gradient.colors)
        }
        binding.layoutColorGradient.ivFilter.setOnClickListener {
            gradientAdapter.clearSelection()
            showGradientColorPicker(mutableListOf())
        }
        binding.layoutColorGradient.tvDirection.setOnClickListener {
            viewModel.toggleGradientDirection()
        }
    }

    private fun showGradientColorPicker(selectedColors: MutableList<Int>) {
        val colorNumber = selectedColors.size + 1
        val positiveText = if (colorNumber < MAX_GRADIENT_COLORS) {
            getString(R.string.txt_next)
        } else {
            getString(R.string.txt_ok)
        }

        ColorPickerDialog.Builder(this, R.style.CustomColorPickerDialog)
            .setTitle("${getString(R.string.txt_gradient)} $colorNumber/$MAX_GRADIENT_COLORS")
            .setPositiveButton(
                positiveText,
                ColorEnvelopeListener { envelope: ColorEnvelope, _: Boolean ->
                    selectedColors.add(envelope.color)
                    if (selectedColors.size == MAX_GRADIENT_COLORS) {
                        viewModel.selectGradient(selectedColors)
                    } else {
                        showGradientColorPicker(selectedColors)
                    }
                }
            )
            .setNegativeButton(
                if (selectedColors.size >= MIN_GRADIENT_COLORS) {
                    getString(R.string.txt_ok)
                } else {
                    getString(R.string.txt_cancel)
                }
            ) { dialog, _ ->
                if (selectedColors.size >= MIN_GRADIENT_COLORS) {
                    viewModel.selectGradient(selectedColors)
                }
                dialog.dismiss()
            }
            .attachAlphaSlideBar(false)
            .attachBrightnessSlideBar(true)
            .show()
    }

    private fun setupControls() {
        binding.ivBack.setOnClickListener { onBack() }

        binding.ivReset.setOnClickListener {
            binding.layoutOverlay.sbBlur.progress = 0
            binding.layoutOverlay.sbOverlay.progress = 0
            viewModel.clearBackground()
            binding.backgroundTheme.blurEnabled = false
            binding.backgroundTheme.overlayColor = Color.TRANSPARENT
            hideBackgroundOptions()
        }

        binding.layoutOverlay.sbBlur.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
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

        binding.layoutOverlay.sbOverlay.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentOverlayAlpha = (progress * 2.55).toInt() // 0-100 to 0-255
                binding.backgroundTheme.overlayColor = Color.argb(currentOverlayAlpha, 0, 0, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnPicture.setOnClickListener {
            updateTabSelection(0)
            viewModel.showPictureBackgrounds()
        }
        binding.btnColor.setOnClickListener {
            updateTabSelection(1)
            showOnlyColorOptions()
        }
        binding.btnGradient.setOnClickListener {
            updateTabSelection(2)
            showOnlyGradientOptions()
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

    private fun ensurePictureBackgroundFragment() {
        if (supportFragmentManager.findFragmentByTag(PICTURE_BG_TAG) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fcvPictureBackground, PictureBGFragment(), PICTURE_BG_TAG)
                .commit()
        }
    }

    private fun showOnlyColorOptions() {
        binding.fcvPictureBackground.isVisible = false
        binding.flBackgroundOptions.isVisible = true
        binding.layoutColor.root.isVisible = true
        binding.layoutOverlay.root.isVisible = false
        binding.layoutColorGradient.root.isVisible = false
    }

    private fun showOnlyGradientOptions() {
        binding.fcvPictureBackground.isVisible = false
        binding.flBackgroundOptions.isVisible = true
        binding.layoutColor.root.isVisible = false
        binding.layoutOverlay.root.isVisible = false
        binding.layoutColorGradient.root.isVisible = true
    }

    private fun showOnlyOverlayOptions() {
        binding.flBackgroundOptions.isVisible = true
        binding.layoutOverlay.root.isVisible = true
        binding.layoutColor.root.isVisible = false
        binding.layoutColorGradient.root.isVisible = false
    }

    private fun hideBackgroundOptions() {
        binding.flBackgroundOptions.isVisible = false
        binding.layoutOverlay.root.isVisible = false
        binding.layoutColor.root.isVisible = false
        binding.layoutColorGradient.root.isVisible = false
    }

    override fun onBack() {
        if (viewModel.isPictureBackgroundVisible.value) {
            viewModel.hidePictureBackgrounds()
            return
        }
        super.onBack()
    }

    override fun initData() = Unit

    override fun initObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.isPictureBackgroundVisible.collect { isVisible ->
                        if (isVisible) ensurePictureBackgroundFragment()
                        binding.fcvPictureBackground.isVisible = isVisible
                    }
                }
                launch {
                    viewModel.backgroundSelection.collect { selection ->
                        applyBackgroundSelection(selection)
                    }
                }
                launch {
                    viewModel.gradientDirection.collect { direction ->
                        gradientAdapter.setDirection(direction)
                        binding.layoutColorGradient.tvDirection.setText(
                            when (direction) {
                                GradientDirection.HORIZONTAL -> R.string.txt_horizontal
                                GradientDirection.VERTICAL -> R.string.txt_vertical
                            }
                        )
                    }
                }
            }
        }
    }

    private fun applyBackgroundSelection(selection: CustomBackgroundSelection) {
        when (selection) {
            CustomBackgroundSelection.Unchanged -> Unit
            CustomBackgroundSelection.Cleared -> {
                binding.backgroundTheme.backgroundBitmap = null
                hideBackgroundOptions()
            }
            is CustomBackgroundSelection.Asset -> {
                ImageUtils.setImageFromAsset(this, selection.path) { bitmap ->
                    if (viewModel.backgroundSelection.value != selection) return@setImageFromAsset
                    binding.backgroundTheme.backgroundBitmap = bitmap
                    showOnlyOverlayOptions()
                }
            }
            is CustomBackgroundSelection.Gallery -> {
                ImageUtils.setImageToBitmap(this, android.net.Uri.parse(selection.uri)) { bitmap ->
                    if (viewModel.backgroundSelection.value != selection) return@setImageToBitmap
                    binding.backgroundTheme.backgroundBitmap = bitmap
                    showOnlyOverlayOptions()
                }
            }
            is CustomBackgroundSelection.SolidColor -> {
                binding.backgroundTheme.backgroundBitmap = null
                binding.backgroundTheme.gradientColors = null
                binding.backgroundTheme.customSolidColor = selection.color
                showOnlyColorOptions()
            }
            is CustomBackgroundSelection.Gradient -> {
                binding.backgroundTheme.backgroundBitmap = null
                binding.backgroundTheme.gradientColors =
                    selection.gradient.colors.toIntArray()
                binding.backgroundTheme.gradientOrientation =
                    when (selection.gradient.direction) {
                        GradientDirection.HORIZONTAL -> {
                            CustomBackgroundView.GradientOrientation.LEFT_RIGHT
                        }
                        GradientDirection.VERTICAL -> {
                            CustomBackgroundView.GradientOrientation.TOP_BOTTOM
                        }
                    }
                showOnlyGradientOptions()
            }
        }
    }

    private companion object {
        const val PICTURE_BG_TAG = "picture_background"
        const val MIN_GRADIENT_COLORS = 2
        const val MAX_GRADIENT_COLORS = 3
    }
}
