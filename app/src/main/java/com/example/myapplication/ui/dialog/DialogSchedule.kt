package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogScheduleBinding

class DialogSchedule(context: Context) :
    BaseDialog<DialogScheduleBinding>(context, DialogScheduleBinding::inflate) {
    var updateOnClick: (() -> Unit)? = null
    var sendNowOnClick: (() -> Unit)? = null
    var deleteOnClick: (() -> Unit)? = null

    override fun initView() {
        super.initView()
        binding.apply {
            btnUpdateMess.setOnClickListener {
                updateOnClick?.invoke()
                dismiss()
            }
            btnSendNow.setOnClickListener {
                sendNowOnClick?.invoke()
                dismiss()
            }
            btnDelete.setOnClickListener {
                deleteOnClick?.invoke()
                dismiss()
            }
        }
    }
}
