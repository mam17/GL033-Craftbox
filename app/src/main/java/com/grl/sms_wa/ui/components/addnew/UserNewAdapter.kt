package com.grl.sms_wa.ui.components.addnew

import com.grl.sms_wa.R
import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemAddUserBinding
import com.grl.sms_wa.domain.layer.DirectoryModel

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
