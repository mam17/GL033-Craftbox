package com.example.myapplication.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseMultiListAdapter<T>(
    diffCallback: DiffUtil.ItemCallback<T>
) : ListAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    private var onItemClick: ((T, Int) -> Unit)? = null

    class MultiViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = getBinding(LayoutInflater.from(parent.context), parent, viewType)
        return MultiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        if (holder is MultiViewHolder) {
            bind(holder.binding, item, position)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(item, position)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getViewType(getItem(position), position)
    }

    abstract fun getBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding

    abstract fun bind(binding: ViewBinding, item: T, position: Int)

    open fun getViewType(item: T, position: Int): Int = 0

    fun setOnItemClick(callback: (T, Int) -> Unit) {
        this.onItemClick = callback
    }
}
