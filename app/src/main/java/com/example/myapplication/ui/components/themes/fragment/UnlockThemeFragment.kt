package com.example.myapplication.ui.components.themes.fragment

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.core.graphics.toColorInt
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentUnlockThemeBinding
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ImageUtils.loadFromPathAction
import com.example.myapplication.utils.ViewEx.applyThemeFont

class UnlockThemeFragment :
    BaseFragment<FragmentUnlockThemeBinding>(FragmentUnlockThemeBinding::inflate) {
    var onUnlockAllClick: ((ThemeMessModel) -> Unit)? = null

    private var theme: ThemeMessModel? = null

    override fun initView() {
        binding.btnUnlockAll.setOnClickListener {
            theme?.let { selectedTheme ->
                onUnlockAllClick?.invoke(selectedTheme)
            }
        }
    }

    override fun initData() {
        theme = readThemeFromArguments()
        theme?.let(::bindThemePreview)
    }

    private fun bindThemePreview(theme: ThemeMessModel) {
        binding.root.applyThemeFont(theme.font)
        bindBackground(theme.pathBG)
        binding.ivMessReceived.loadFromPathAction(
            "file:///android_asset/${theme.pathBubbleReceived}",
            isCenterCrop = false
        )
        binding.ivMessSent.loadFromPathAction(
            "file:///android_asset/${theme.pathBubbleSent}",
            isCenterCrop = false
        )
        binding.ivUnlock.loadFromPathAction(
            "file:///android_asset/${theme.pathAvt}",
            isCenterCrop = false
        )
        binding.ivMessComposer.loadFromPathAction(
            "file:///android_asset/${theme.pathEnterChat}",
            isCenterCrop = false
        )
    }

    private fun bindBackground(backgroundValue: String) {
        if (backgroundValue.startsWith("#")) {
            binding.ivBGTheme.setImageDrawable(null)
            binding.ivBGTheme.setBgColor(backgroundValue.toColorInt())
        } else {
            binding.ivBGTheme.setBgColor(Color.TRANSPARENT)
            binding.ivBGTheme.loadFromPathAction(
                "file:///android_asset/$backgroundValue",
                isCenterCrop = true
            )
        }
    }

    private fun readThemeFromArguments(): ThemeMessModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(Constant.EXTRA_THEME, ThemeMessModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(Constant.EXTRA_THEME)
        }
    }

    companion object {
        fun newInstance(theme: ThemeMessModel): UnlockThemeFragment {
            return UnlockThemeFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(Constant.EXTRA_THEME, theme)
                }
            }
        }
    }
}
