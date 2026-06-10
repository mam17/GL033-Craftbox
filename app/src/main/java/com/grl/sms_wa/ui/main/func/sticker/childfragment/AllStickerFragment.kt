package com.grl.sms_wa.ui.main.func.sticker.childfragment

import android.os.Bundle
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentAllStickerBinding
import com.grl.sms_wa.domain.layer.StickerModel
import com.grl.sms_wa.ui.components.detail_sticker.DetailStickerActivity
import com.grl.sms_wa.ui.main.func.sticker.StickerViewModel
import com.grl.sms_wa.ui.main.func.sticker.adapter.StickerAdapter
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.gone

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
