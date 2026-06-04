package com.example.myapplication.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseMultiAdapter<T> : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var dataList = mutableListOf<T>()
    private var onItemClick: ((T, Int) -> Unit)? = null

    class MultiViewHolder(val binding: ViewBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = getBinding(LayoutInflater.from(parent.context), parent, viewType)
        return MultiViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = dataList[position]
        if (holder is MultiViewHolder) {
            bind(holder.binding, item, position)
            holder.itemView.setOnClickListener {
                onItemClick?.invoke(item, position)
            }
        }
    }

    override fun getItemCount(): Int = dataList.size

    override fun getItemViewType(position: Int): Int {
        return getViewType(dataList[position], position)
    }

    abstract fun getBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding

    abstract fun bind(binding: ViewBinding, item: T, position: Int)

    open fun getViewType(item: T, position: Int): Int = 0

    fun setData(data: List<T>) {
        dataList.clear()
        dataList.addAll(data)
        notifyDataSetChanged()
    }

    fun getData(): List<T> = dataList

    fun addData(data: List<T>) {
        val startPos = dataList.size
        dataList.addAll(data)
        notifyItemRangeInserted(startPos, data.size)
    }

    fun removeData() {
        dataList.clear()
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        if (position in dataList.indices) {
            dataList.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataList.size)
        }
    }

    fun setOnItemClick(callback: (T, Int) -> Unit) {
        this.onItemClick = callback
    }
}
