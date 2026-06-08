package com.example.myapplication.ui.components.addnew

import com.example.myapplication.R
import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemAddUserBinding
import com.example.myapplication.domain.layer.DirectoryModel

class UserNewAdapter : BaseAdapter<DirectoryModel, ItemAddUserBinding>(ItemAddUserBinding::inflate) {
    override fun bind(binding: ItemAddUserBinding, item: DirectoryModel, position: Int) {
        binding.tvName.text = item.strName.ifBlank { item.strPhone }
        if (item.photo != null) {
            binding.ivAvatar.setImageBitmap(item.photo)
            binding.ivAvatar.clearColorFilter()
        } else {
            binding.ivAvatar.setImageResource(R.drawable.ic_person)
        }
    }
}
