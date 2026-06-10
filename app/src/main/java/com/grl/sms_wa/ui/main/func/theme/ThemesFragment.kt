package com.grl.sms_wa.ui.main.func.theme

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentThemesBinding
import com.grl.sms_wa.ui.components.themes.activity.GetThemesActivity
import com.grl.sms_wa.ui.components.themes.fragment.SettingThemeFragment
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.ui.main.func.theme.adapter.CategoryThemeAdapter
import com.grl.sms_wa.ui.main.func.theme.adapter.ThemeAdapter
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.ViewEx.tintColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemesFragment : BaseFragment<FragmentThemesBinding>(FragmentThemesBinding::inflate) {
    private val viewModel: ThemesViewModel by viewModels()
    private val categoryAdapter = CategoryThemeAdapter()
    private val themeAdapter = ThemeAdapter()
    private val spManager by lazy { SpManager.get(requireContext()) }
    private val themeDetailLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                (activity as? MainActivity)?.refreshCurrentMainFragmentForTheme()
            }
        }

    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
        binding.rcvCategoryTheme.adapter = categoryAdapter
        binding.rvThemes.adapter = themeAdapter
        binding.ivSetting.setOnClickListener {
            (activity as? MainActivity)?.showOverlayFeatureFragment(SettingThemeFragment())
        }
        categoryAdapter.setOnItemClick { _, position ->
            viewModel.selectCategory(position)
            categoryAdapter.setSelectedPosition(position)
        }
        themeAdapter.setOnItemClick { theme, position ->
            val themes = viewModel.themesOfSelectedCategory.value
            if (themes.isEmpty()) {
                showToast(getString(R.string.txt_no_theme_data_available))
                return@setOnItemClick
            }
            themeDetailLauncher.launch(
                Intent(requireContext(), GetThemesActivity::class.java).apply {
                    putExtra(
                        Constant.EXTRA_THEME_CATEGORY_NAME,
                        viewModel.categoriesState.value
                            .getOrNull(viewModel.selectedCategoryIndex.value)
                            ?.nameCategory
                    )
                    putParcelableArrayListExtra(Constant.EXTRA_THEME_LIST, ArrayList(themes))
                    putExtra(Constant.EXTRA_THEME_SELECTED_POSITION, position)
                }
            )
        }
    }

    private fun applySystemBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun initObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categoriesState.collect { categories ->
                        categoryAdapter.setData(categories)
                    }
                }
                launch {
                    viewModel.selectedCategoryIndex.collect { position ->
                        categoryAdapter.setSelectedPosition(position)
                    }
                }
                launch {
                    viewModel.themesOfSelectedCategory.collect { themes ->
                        themeAdapter.setData(themes)
                        categoryAdapter.setTheme(themes.firstOrNull())
                        updateNoData()
                    }
                }
            }
        }

    }

    private fun updateNoData() {
        binding.llNoData.llNoData.isVisible = themeAdapter.getData().isEmpty()
        binding.llNoData.prLoading.gone()
        binding.llNoData.tvBodyNoData.text = getString(R.string.txt_no_theme_data_available)
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        binding.root.applyThemeFont(theme.font)
        binding.tvTitle.setTextColor(theme.colMain.toColorInt())
        binding.llNoData.tvBodyNoData.setTextColor(theme.colMain.toColorInt())
        binding.ivSetting.tintColor(theme.colMain.toColorInt())
    }
}
