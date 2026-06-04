package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogLoadingBinding

class DialogLoading(context: Context, private val strTitle: String) :
    BaseDialog<DialogLoadingBinding>(context, DialogLoadingBinding::inflate) {

    override fun initView() {
        super.initView()
        setCancelable(false)
        binding.apply {
            tvTitleLoading.text = strTitle
        }
    }
}