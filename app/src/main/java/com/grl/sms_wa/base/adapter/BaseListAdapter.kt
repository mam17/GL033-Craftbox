package com.grl.sms_wa.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.grl.sms_wa.utils.ViewEx.applyCurrentThemeFont

abstract class BaseListAdapter<T, VB : ViewBinding>(
    diffCallback: DiffUtil.ItemCallback<T>,
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB
) : ListAdapter<T, BaseListAdapter.BaseViewHolder<VB>>(diffCallback) {

    private var onItemClick: ((T, Int) -> Unit)? = null

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = getItem(position)
        holder.binding.root.applyCurrentThemeFont()
        bind(holder.binding, item, position)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }
    }

    abstract fun bind(binding: VB, item: T, position: Int)

    fun setOnItemClick(callback: (T, Int) -> Unit) {
        this.onItemClick = callback
    }
}
