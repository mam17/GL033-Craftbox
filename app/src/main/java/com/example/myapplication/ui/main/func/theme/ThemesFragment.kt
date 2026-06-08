package com.example.myapplication.ui.main.func.theme

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentThemesBinding

class ThemesFragment : BaseFragment<FragmentThemesBinding>(FragmentThemesBinding::inflate) {
    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
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
}