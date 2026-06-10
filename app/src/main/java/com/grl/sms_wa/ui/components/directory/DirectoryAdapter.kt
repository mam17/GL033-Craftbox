package com.grl.sms_wa.ui.components.directory

import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemDirectoryBinding
import com.grl.sms_wa.domain.layer.DirectoryModel
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.utils.ViewEx.applyThemeFont

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