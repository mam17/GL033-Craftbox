package com.grl.sms_wa.ui.components.addnew

import androidx.core.graphics.toColorInt
import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemAddUserBinding
import com.grl.sms_wa.domain.layer.DirectoryModel
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.utils.AppEx.dpToPx
import com.grl.sms_wa.utils.ImageUtils.setImageFromAsset
import com.grl.sms_wa.utils.ViewEx.applyThemeFont

class AddUserAdapter :
    BaseAdapter<DirectoryModel, ItemAddUserBinding>(ItemAddUserBinding::inflate) {
    private var mThemeModel: ThemeMessModel? = null
    var onRemove: ((DirectoryModel) -> Unit)? = null

    fun setTheme(theme: ThemeMessModel?) {
        this.mThemeModel = theme
        notifyDataSetChanged()
    }

    override fun bind(
        binding: ItemAddUserBinding,
        item: DirectoryModel,
        position: Int
    ) {
        binding.apply {
            val context = root.context

            tvName.text = item.strName
            if (item.photo != null) {
                ivAvatar.setImageBitmap(item.photo)
                ivAvatar.clearColorFilter()
            } else {
                mThemeModel?.let { theme ->
                    val padding = context.dpToPx(4)
                    ivAvatar.setPadding(padding, padding, padding, padding)
                    ivAvatar.setImageResource(R.drawable.ic_person)
                    ivAvatar.setBgColor(theme.colMain.toColorInt())
                    setImageFromAsset(
                        context,
                        theme.pathAvt,
                        onBitmapReady = {
                            ivAvatar.setBitmap(it)
                        })
                }
            }

            mThemeModel?.let { theme ->
                root.applyThemeFont(theme.font)
                root.setCardBackgroundColor(theme.colMain.toColorInt())
            }

            btnRemove.setOnClickListener {
                onRemove?.invoke(item)
            }
        }
    }
}
