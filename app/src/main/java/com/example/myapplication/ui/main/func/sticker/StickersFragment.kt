package com.example.myapplication.ui.main.func.sticker

import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentStickersBinding
import com.example.myapplication.ui.main.func.sticker.adapter.StickerPagerAdapter
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.applyThemeFont
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StickersFragment : BaseFragment<FragmentStickersBinding>(FragmentStickersBinding::inflate) {
    private val viewModel: StickerViewModel by viewModels()
    private val spManager by lazy { SpManager.get(requireContext()) }

    private val stickerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateSelectedTab(position)
        }
    }

    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
        binding.vpSticker.apply {
            adapter = StickerPagerAdapter(this@StickersFragment)
            offscreenPageLimit = 2
            registerOnPageChangeCallback(stickerPageChangeCallback)
        }

        binding.btnAll.setOnClickListener {
            binding.vpSticker.currentItem = PAGE_ALL
        }
        binding.btnYourSticker.setOnClickListener {
            binding.vpSticker.currentItem = PAGE_YOUR_STICKER
        }
        updateSelectedTab(PAGE_ALL)
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
        viewModel.loadStickers()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
        viewModel.loadStickers()
    }

    override fun onDestroyView() {
        binding.vpSticker.unregisterOnPageChangeCallback(stickerPageChangeCallback)
        super.onDestroyView()
    }

    private fun updateSelectedTab(position: Int) {
        val mainColor = spManager.getCurrentTheme()?.colMain?.toColorInt()
            ?: ContextCompat.getColor(requireContext(), R.color.col_main)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val isAllSelected = position == PAGE_ALL

        binding.btnAll.isSelected = isAllSelected
        binding.btnYourSticker.isSelected = !isAllSelected

        binding.btnAll.setCardBackgroundColor(if (isAllSelected) mainColor else whiteColor)
        binding.tvAll.setTextColor(if (isAllSelected) whiteColor else mainColor)
        binding.btnAll.strokeColor = mainColor

        binding.btnYourSticker.setCardBackgroundColor(if (isAllSelected) whiteColor else mainColor)
        binding.tvYourSticker.setTextColor(if (isAllSelected) mainColor else whiteColor)
        binding.btnYourSticker.strokeColor = mainColor
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        binding.root.applyThemeFont(theme.font)
        binding.tvTitle.setTextColor(theme.colMain.toColorInt())
        updateSelectedTab(binding.vpSticker.currentItem)
    }

    companion object {
        private const val PAGE_ALL = 0
        private const val PAGE_YOUR_STICKER = 1
    }
}
