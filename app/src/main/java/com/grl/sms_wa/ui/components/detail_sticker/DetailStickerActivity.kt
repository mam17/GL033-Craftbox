package com.grl.sms_wa.ui.components.detail_sticker

import android.os.Build
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityDetailStickerBinding
import com.grl.sms_wa.domain.layer.StickerModel
import com.grl.sms_wa.ui.main.func.sticker.adapter.StickerDetailAdapter
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.ImageUtils.loadFromPathAction
import com.grl.sms_wa.utils.ViewEx.gone
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
