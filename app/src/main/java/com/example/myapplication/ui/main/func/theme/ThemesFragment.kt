package com.example.myapplication.ui.main.func.theme

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentThemesBinding
import com.example.myapplication.ui.components.themes.activity.GetThemesActivity
import com.example.myapplication.ui.main.func.theme.adapter.CategoryThemeAdapter
import com.example.myapplication.ui.main.func.theme.adapter.ThemeAdapter
import com.example.myapplication.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ThemesFragment : BaseFragment<FragmentThemesBinding>(FragmentThemesBinding::inflate) {
    private val viewModel: ThemesViewModel by viewModels()
    private val categoryAdapter = CategoryThemeAdapter()
    private val themeAdapter = ThemeAdapter()

    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
        binding.rcvCategoryTheme.adapter = categoryAdapter
        binding.rvThemes.adapter = themeAdapter

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
            startNextActivity(
                GetThemesActivity::class.java,
                android.os.Bundle().apply {
                    putString(
                        Constant.EXTRA_THEME_CATEGORY_NAME,
                        viewModel.categoriesState.value
                            .getOrNull(viewModel.selectedCategoryIndex.value)
                            ?.nameCategory
                    )
                    putParcelableArrayList(Constant.EXTRA_THEME_LIST, ArrayList(themes))
                    putInt(Constant.EXTRA_THEME_SELECTED_POSITION, position)
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
                    }
                }
            }
        }
    }
}
