package com.example.myapplication.base.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BaseAdapter<T, VB : ViewBinding>(
    private val bindingInflater: (LayoutInflater, ViewGroup, Boolean) -> VB
) : RecyclerView.Adapter<BaseAdapter.BaseViewHolder<VB>>() {

    protected var dataList = mutableListOf<T>()
    private var onItemClick: ((T, Int) -> Unit)? = null

    class BaseViewHolder<VB : ViewBinding>(val binding: VB) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<VB> {
        val binding = bindingInflater(LayoutInflater.from(parent.context), parent, false)
        return BaseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BaseViewHolder<VB>, position: Int) {
        val item = dataList[position]
        bind(holder.binding, item, position)
        holder.itemView.setOnClickListener {
            onItemClick?.invoke(item, position)
        }
    }

    override fun getItemCount(): Int = dataList.size

    abstract fun bind(binding: VB, item: T, position: Int)

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

    /**
     * Hàm này có thể được override nếu logic chọn item phức tạp hơn
     */
    open fun selectItem(position: Int) {
        // Thực hiện logic trong adapter cụ thể vì model T là generic
    }
}