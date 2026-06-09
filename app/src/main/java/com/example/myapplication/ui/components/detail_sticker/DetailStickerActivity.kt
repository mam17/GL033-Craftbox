package com.example.myapplication.ui.components.detail_sticker

import android.os.Build
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityDetailStickerBinding
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.ui.main.func.sticker.adapter.StickerDetailAdapter
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ImageUtils.loadFromPathAction
import com.example.myapplication.utils.ViewEx.gone
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailStickerActivity :
    BaseActivity<ActivityDetailStickerBinding>(ActivityDetailStickerBinding::inflate) {

    private val stickerDetailAdapter = StickerDetailAdapter()
    private var sticker: StickerModel? = null

    override fun initView() {
        sticker = readStickerFromIntent()

        binding.toolbarDTSticker.btnAction.gone()
        binding.toolbarDTSticker.btnSelect.gone()
        binding.toolbarDTSticker.btnBack.setOnClickListener { onBack() }
        binding.tvDownload.setTextColor(ContextCompat.getColor(this, R.color.white))

        binding.rcvStickerStore.apply {
            layoutManager = GridLayoutManager(this@DetailStickerActivity, 3)
            adapter = stickerDetailAdapter
        }

        binding.btnDownload.setOnClickListener {
            sticker?.let { selectedSticker ->
                spManager.setStickerAdded(selectedSticker.name, true)
                updateDownloadState(isAdded = true)
            }
        }
    }

    override fun initData() {
        val selectedSticker = sticker ?: run {
            onBack()
            return
        }

        binding.toolbarDTSticker.tvTitle.text = selectedSticker.name
        binding.ivStickerLogo.loadFromPathAction(
            "file:///android_asset/${selectedSticker.previewPath}",
            isCenterCrop = false
        )
        stickerDetailAdapter.setData(selectedSticker.detailPaths)
        updateDownloadState(spManager.isStickerAdded(selectedSticker.name))
    }

    private fun readStickerFromIntent(): StickerModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Constant.EXTRA_STICKER, StickerModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(Constant.EXTRA_STICKER)
        }
    }

    private fun updateDownloadState(isAdded: Boolean) {
        binding.tvDownload.text = getString(
            if (isAdded) {
                R.string.txt_downloaded
            } else {
                R.string.txt_download
            }
        )
        binding.btnDownload.isEnabled = isAdded.not()
        binding.ivDone.visibility = if (isAdded) {
            android.view.View.VISIBLE
        } else {
            android.view.View.GONE
        }
    }
}
