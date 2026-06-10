package com.example.myapplication.ui.components.themes.adapter

import android.view.View
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemChooseThemeBinding
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.ImageUtils.loadFromPathAction
import com.example.myapplication.views.RoundImageView

class ChooseThemePagerAdapter :
    BaseAdapter<ThemeMessModel, ItemChooseThemeBinding>(ItemChooseThemeBinding::inflate) {

    var onDownloadClick: ((ThemeMessModel, Int) -> Unit)? = null

    private var downloadedThemeKeys: Set<String> = emptySet()
    private var loadingPosition: Int? = null
    private var mainColor: Int? = null

    override fun bind(
        binding: ItemChooseThemeBinding,
        item: ThemeMessModel,
        position: Int
    ) {
        val pathImage = "file:///android_asset/${item.pathThemePreview}"
        val isDownloaded = downloadedThemeKeys.contains(item.downloadKey())
        val isLoading = loadingPosition == position

        if (binding.ivChooseTheme.tag != pathImage) {
            binding.ivChooseTheme.tag = pathImage
            binding.ivChooseTheme.loadFromPathAction(pathImage, isCenterCrop = false)
        }
        binding.prLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.ivDownload.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.ivDownload.setImageResource(if (isDownloaded) R.drawable.ic_check else R.drawable.ic_download)
        binding.ivDownload.applyThemeStyle(mainColor)
        binding.prLoading.indeterminateTintList = mainColor?.let { android.content.res.ColorStateList.valueOf(it) }
        binding.ivDownload.setOnClickListener {
            onDownloadClick?.invoke(item, position)
        }
    }

    fun setDownloadedThemeKeys(keys: Set<String>) {
        val changedKeys = (downloadedThemeKeys - keys) + (keys - downloadedThemeKeys)
        downloadedThemeKeys = keys
        if (changedKeys.isEmpty()) return

        dataList.forEachIndexed { index, theme ->
            if (theme.downloadKey() in changedKeys) {
                notifyItemChanged(index)
            }
        }
    }

    fun showLoading(position: Int) {
        val oldPosition = loadingPosition
        loadingPosition = position
        oldPosition?.let(::notifyItemChanged)
        notifyItemChanged(position)
    }

    fun hideLoading(position: Int) {
        loadingPosition = null
        notifyItemChanged(position)
    }

    fun setMainColor(color: Int?) {
        if (mainColor == color) return
        mainColor = color
        notifyDataSetChanged()
    }

    private fun RoundImageView.applyThemeStyle(color: Int?) {
        if (color == null) return
        setStroke(true, color, resources.getDimension(R.dimen.size1))
        setIconTint(color)
    }

    private fun ThemeMessModel.downloadKey(): String {
        return "${pathThemePreview}_$id"
    }
}
