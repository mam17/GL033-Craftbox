package com.grl.sms_wa.ui.dialog

import android.content.Context
import com.grl.sms_wa.base.dialog.BaseDialog
import com.grl.sms_wa.databinding.DialogScheduleBinding

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
