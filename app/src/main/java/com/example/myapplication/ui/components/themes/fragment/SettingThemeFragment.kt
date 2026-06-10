package com.example.myapplication.ui.components.themes.fragment

import android.view.View
import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentSettingThemeBinding
import com.example.myapplication.ui.components.custom.activity.CustomBGThemeActivity
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.applyThemeFont
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.setTint
import com.example.myapplication.utils.ViewEx.tintColor

class SettingThemeFragment : BaseFragment<FragmentSettingThemeBinding>(FragmentSettingThemeBinding::inflate) {
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.apply{
            toolbarSettingTheme.apply {
                btnBack.setOnClickListener { removeFragment(this@SettingThemeFragment) }
                btnAction.gone()
                btnSelect.gone()
                tvTitle.text = getString(R.string.txt_themes)
            }
            btnBackground.setOnClickListener {
                startNextActivity(CustomBGThemeActivity::class.java)
            }
            btnBubble.setOnClickListener {
                showToast("Bubble")
            }
            btnFont.setOnClickListener {
                showToast("Font")
            }
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        val mainColor = theme.colMain.toColorInt()

        binding.root.applyThemeFont(theme.font)
        binding.toolbarSettingTheme.tvTitle.setTextColor(mainColor)
        binding.toolbarSettingTheme.btnBack.tintColor(mainColor)
        binding.btnBackground.setTint(mainColor)
        binding.btnBubble.setTint(mainColor)
        binding.btnFont.setTint(mainColor)

    }

}
