package com.grl.sms_wa.ui.dialog

import android.content.Context
import com.grl.sms_wa.base.dialog.BaseDialog
import com.grl.sms_wa.databinding.DialogLoadingBinding

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