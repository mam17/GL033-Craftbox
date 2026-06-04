package com.example.myapplication.base.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.viewbinding.ViewBinding

abstract class BaseDialog<VB : ViewBinding>(
    context: Context,
    private val bindingInflater: (LayoutInflater) -> VB
) : Dialog(context) {

    protected val binding: VB by lazy { bindingInflater(layoutInflater) }
    var okOnClick: (() -> Unit)? = null
    var cancelOnClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        window?.apply {
            setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setGravity(Gravity.CENTER)
        }
        initView()
        initData()
    }

    open fun initView() {}
    open fun initData() {}
}