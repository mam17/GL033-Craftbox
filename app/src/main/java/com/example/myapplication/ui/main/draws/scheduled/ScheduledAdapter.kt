package com.example.myapplication.ui.main.draws.scheduled

import androidx.recyclerview.widget.DiffUtil
import com.example.myapplication.base.adapter.BaseListAdapter
import com.example.myapplication.databinding.ItemScheduledBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScheduledAdapter : BaseListAdapter<ScheduledMessageItem, ItemScheduledBinding>(
    DIFF_CALLBACK,
    ItemScheduledBinding::inflate
) {
    private val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())

    override fun bind(binding: ItemScheduledBinding, item: ScheduledMessageItem, position: Int) {
        binding.tvAddressScheduled.text = item.displayName
        binding.tvBodyScheduled.text = item.body
        binding.tvTimeScheduled.text = dateFormat.format(Date(item.scheduledTime))
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ScheduledMessageItem>() {
            override fun areItemsTheSame(
                oldItem: ScheduledMessageItem,
                newItem: ScheduledMessageItem
            ): Boolean = oldItem.id == newItem.id

            override fun areContentsTheSame(
                oldItem: ScheduledMessageItem,
                newItem: ScheduledMessageItem
            ): Boolean = oldItem == newItem
        }
    }
}
