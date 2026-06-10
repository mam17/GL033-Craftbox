package com.example.myapplication.ui.main.func.sticker.childfragment

import android.os.Bundle
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentAllStickerBinding
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.ui.components.detail_sticker.DetailStickerActivity
import com.example.myapplication.ui.main.func.sticker.StickerViewModel
import com.example.myapplication.ui.main.func.sticker.adapter.StickerAdapter
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.gone

class AllStickerFragment : BaseFragment<FragmentAllStickerBinding>(FragmentAllStickerBinding::inflate) {

    private val viewModel: StickerViewModel by viewModels({ requireParentFragment() })
    private val stickerAdapter = StickerAdapter()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.rcvAllSticker.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = stickerAdapter
        }
        stickerAdapter.setOnItemClick { sticker, _ ->
            startNextActivity(
                DetailStickerActivity::class.java,
                sticker.toBundle()
            )
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    override fun initObserver() {
        viewModel.allStickers.observe(viewLifecycleOwner) { stickers ->
            stickerAdapter.setData(stickers)
            binding.llNoData.llNoData.isVisible = stickers.isEmpty()
            binding.llNoData.prLoading.gone()
            binding.llNoData.tvBodyNoData.text = getString(R.string.txt_no_sticker_data_available)
        }
    }

    private fun StickerModel.toBundle(): Bundle {
        return Bundle().apply {
            putParcelable(Constant.EXTRA_STICKER, this@toBundle)
        }
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        binding.llNoData.tvBodyNoData.setTextColor(theme.colMain.toColorInt())
        binding.llNoData.prLoading.indeterminateTintList =
            android.content.res.ColorStateList.valueOf(theme.colMain.toColorInt())
    }
}
