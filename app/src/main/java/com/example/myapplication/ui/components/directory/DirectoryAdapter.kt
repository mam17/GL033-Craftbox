package com.example.myapplication.ui.components.directory

import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemDirectoryBinding
import com.example.myapplication.domain.layer.DirectoryModel
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.ViewEx.applyThemeFont

class DirectoryAdapter :
    BaseAdapter<DirectoryModel, ItemDirectoryBinding>(ItemDirectoryBinding::inflate) {
    private var mThemeModel: ThemeMessModel? = null

    fun setTheme(theme: ThemeMessModel?) {
        this.mThemeModel = theme
        notifyDataSetChanged()
    }

    override fun bind(
        binding: ItemDirectoryBinding,
        item: DirectoryModel,
        position: Int
    ) {
        binding.apply {
            tvNameDirectory.text = item.strName
            tvNumberPhone.text = item.strPhone

            if (item.photo != null) {
                ivAvatar.setImageBitmap(item.photo)
                ivAvatar.clearColorFilter()
            } else {
                ivAvatar.setImageResource(R.drawable.ic_person)
            }

            val context = root.context
            mThemeModel?.let { theme ->
                root.applyThemeFont(theme.font)
                tvNameDirectory.setTextColor(theme.colMain.toColorInt())
                tvNumberPhone.setTextColor(theme.colMain.toColorInt())
                if (item.photo == null) {
                    ivAvatar.setColorFilter(theme.colMain.toColorInt())
                }
            } ?: run {
                tvNameDirectory.setTextColor(ContextCompat.getColor(context, R.color.black))
                tvNumberPhone.setTextColor(ContextCompat.getColor(context, R.color.gray))
                if (item.photo == null) {
                    ivAvatar.setColorFilter(ContextCompat.getColor(context, R.color.col_main))
                }
            }

        }
    }
}