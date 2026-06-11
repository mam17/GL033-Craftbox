package com.grl.sms_wa.ui.components.themes.fragment

import androidx.core.graphics.toColorInt
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentSettingThemeBinding
import com.grl.sms_wa.ui.components.custom.activity.CustomBGThemeActivity
import com.grl.sms_wa.ui.components.custom.activity.CustomBubbleActivity
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.ViewEx.setTint
import com.grl.sms_wa.utils.ViewEx.tintColor

class SettingThemeFragment :
    BaseFragment<FragmentSettingThemeBinding>(FragmentSettingThemeBinding::inflate) {
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.apply {
            toolbarSettingTheme.apply {
                btnBack.setOnClickListener {
                    (activity as? MainActivity)?.closeDrawerFeatureFragment(this@SettingThemeFragment)
                        ?: removeFragment(this@SettingThemeFragment) }
                btnAction.gone()
                btnSelect.gone()
                tvTitle.text = getString(R.string.txt_themes)
            }
            btnBackground.setOnClickListener {
                startNextActivity(CustomBGThemeActivity::class.java)
            }
            btnBubble.setOnClickListener {
                startNextActivity(CustomBubbleActivity::class.java)
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
