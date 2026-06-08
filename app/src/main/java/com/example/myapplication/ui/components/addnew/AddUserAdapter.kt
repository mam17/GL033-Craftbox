package com.example.myapplication.ui.components.addnew

import androidx.core.graphics.toColorInt
import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemAddUserBinding
import com.example.myapplication.domain.layer.DirectoryModel
import com.example.myapplication.domain.layer.ThemeMessModel
import com.example.myapplication.utils.AppEx.dpToPx
import com.example.myapplication.utils.ImageUtils.setImageFromAsset
import com.example.myapplication.utils.ViewEx.applyThemeFont

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
                    ivAvatar.setColorFilter(theme.colTextBBSent.toColorInt())
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
