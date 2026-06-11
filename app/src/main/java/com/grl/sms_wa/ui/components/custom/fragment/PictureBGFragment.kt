package com.grl.sms_wa.ui.components.custom.fragment

import android.content.Intent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentPictureBinding
import com.grl.sms_wa.ui.components.custom.adapter.BackgroundThemeAdapter
import com.grl.sms_wa.ui.components.custom.adapter.BackgroundThemeItem
import com.grl.sms_wa.ui.components.custom.viewmodel.CustomBGViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PictureBGFragment :
    BaseFragment<FragmentPictureBinding>(FragmentPictureBinding::inflate) {

    private val viewModel: CustomBGViewModel by activityViewModels()
    private val backgroundAdapter by lazy { BackgroundThemeAdapter() }

    private val openImageLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@registerForActivityResult
        runCatching {
            requireContext().contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        viewModel.selectGalleryBackground(uri.toString())
    }

    override fun initView() {
        applyStatusBarInset()
        binding.btnBack.setOnClickListener {
            viewModel.hidePictureBackgrounds()
        }
        binding.rcvBackground.apply {
            layoutManager = GridLayoutManager(context, BACKGROUND_SPAN_COUNT)
            adapter = backgroundAdapter
        }
        backgroundAdapter.setOnItemClick { item, _ ->
            when (item) {
                BackgroundThemeItem.Library -> openImageLauncher.launch(arrayOf("image/*"))
                is BackgroundThemeItem.Asset -> {
                    viewModel.selectAssetBackground(item.path)
                }
            }
        }
    }

    private fun applyStatusBarInset() {
        val initialTopPadding = binding.llHeader.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(binding.llHeader) { header, insets ->
            val statusBarTop = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top
            header.setPadding(
                header.paddingLeft,
                initialTopPadding + statusBarTop,
                header.paddingRight,
                header.paddingBottom
            )
            insets
        }
        ViewCompat.requestApplyInsets(binding.llHeader)
    }

    override fun initData() {
        val items = buildList {
            add(BackgroundThemeItem.Library)
            addAll(findBackgroundAssets().map(BackgroundThemeItem::Asset))
        }
        backgroundAdapter.setData(items)
    }

    private fun findBackgroundAssets(): List<String> {
        val assetManager = requireContext().assets

        fun scan(path: String): List<String> {
            val children = assetManager.list(path).orEmpty()
            if (children.isEmpty()) {
                return if (path.substringAfterLast('/').startsWith("bg_")) listOf(path) else emptyList()
            }
            return children.flatMap { child -> scan("$path/$child") }
        }

        return scan(THEMES_PATH).sorted()
    }

    private companion object {
        const val THEMES_PATH = "themes"
        const val BACKGROUND_SPAN_COUNT = 2
    }
}
