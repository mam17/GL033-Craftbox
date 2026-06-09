package com.example.myapplication.ui.main.func.sticker

import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentStickersBinding
import com.example.myapplication.ui.main.func.sticker.adapter.StickerPagerAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StickersFragment : BaseFragment<FragmentStickersBinding>(FragmentStickersBinding::inflate) {
    private val viewModel: StickerViewModel by viewModels()

    private val stickerPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateSelectedTab(position)
        }
    }

    override fun initView() {
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

    override fun initData() {
        viewModel.loadStickers()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadStickers()
    }

    override fun onDestroyView() {
        binding.vpSticker.unregisterOnPageChangeCallback(stickerPageChangeCallback)
        super.onDestroyView()
    }

    private fun updateSelectedTab(position: Int) {
        val mainColor = ContextCompat.getColor(requireContext(), R.color.col_main)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val isAllSelected = position == PAGE_ALL

        binding.btnAll.isSelected = isAllSelected
        binding.btnYourSticker.isSelected = !isAllSelected

        binding.btnAll.setCardBackgroundColor(if (isAllSelected) mainColor else whiteColor)
        binding.tvAll.setTextColor(if (isAllSelected) whiteColor else mainColor)

        binding.btnYourSticker.setCardBackgroundColor(if (isAllSelected) whiteColor else mainColor)
        binding.tvYourSticker.setTextColor(if (isAllSelected) mainColor else whiteColor)
    }

    companion object {
        private const val PAGE_ALL = 0
        private const val PAGE_YOUR_STICKER = 1
    }
}
