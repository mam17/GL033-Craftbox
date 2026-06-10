package com.example.myapplication.ui.components.mess.fragment

import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentChooseStickerBinding
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.ui.components.mess.adapter.CateStickerAdapter
import com.example.myapplication.ui.components.mess.adapter.ChooseStickerAdapter
import com.example.myapplication.ui.main.func.sticker.StickerAssetLoader
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.gone

class ChooseStickerFragment :
    BaseFragment<FragmentChooseStickerBinding>(FragmentChooseStickerBinding::inflate) {
    var onStickerClick: ((String) -> Unit)? = null
    private var mCateStickerAdapter = CateStickerAdapter()
    private var mChooseStickerAdapter = ChooseStickerAdapter()
    private var stickerPacks = emptyList<StickerModel>()

    override fun initView() {
        binding.rcvCateSticker.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = mCateStickerAdapter
        }
        binding.rcvChooseSticker.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = mChooseStickerAdapter
        }

        mCateStickerAdapter.setOnItemClick { _, position ->
            mCateStickerAdapter.setSelected(position)
            showStickerPack(position)
        }
        mChooseStickerAdapter.setOnItemClick { stickerPath, _ ->
            onStickerClick?.invoke(stickerPath)
        }
    }

    override fun initData() {
        mCateStickerAdapter.setTheme(SpManager.get(requireContext()).getCurrentTheme())
        val addedStickerNames = SpManager.get(requireContext()).getAddedStickerNames().toSet()
        stickerPacks = StickerAssetLoader.loadStickerCategories(requireContext().assets)
            .filter { addedStickerNames.contains(it.name) }

        mCateStickerAdapter.setData(stickerPacks.map { it.name })
        showStickerPack(position = 0)

        binding.llNoData.prLoading.gone()
        binding.llNoData.llNoData.isVisible = stickerPacks.isEmpty()
        binding.llNoData.tvBodyNoData.text = getString(R.string.txt_no_sticker_data_available)
    }

    private fun showStickerPack(position: Int) {
        mChooseStickerAdapter.setData(stickerPacks.getOrNull(position)?.detailPaths.orEmpty())
    }
}
