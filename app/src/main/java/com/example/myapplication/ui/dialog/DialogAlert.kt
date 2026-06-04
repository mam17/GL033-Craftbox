package com.example.myapplication.ui.dialog

import android.content.Context
import com.example.myapplication.base.dialog.BaseDialog
import com.example.myapplication.databinding.DialogAlertBinding
import kotlin.apply
import kotlin.let
import kotlin.takeIf
import kotlin.text.isNotEmpty

class DialogAlert(
    context: Context,
    private val strTitle: String? = null,
    private val strBody: String? = null,
    private val strCancel: String? = null,
    private val strYes: String? = null
) : BaseDialog<DialogAlertBinding>(context,DialogAlertBinding::inflate) {
    override fun initView() {
        super.initView()
        binding.apply {
            strTitle?.takeIf { it.isNotEmpty() }?.let { tvTitle.text = it }
            strBody?.takeIf { it.isNotEmpty() }?.let { tvBody.text = it }
            strYes?.takeIf { it.isNotEmpty() }?.let { btnYes.text = it }
            strCancel?.takeIf { it.isNotEmpty() }?.let { btnCancel.text = it }

            btnCancel.setOnClickListener {
                cancelOnClick?.invoke()
                dismiss()
            }
            btnYes.setOnClickListener {
                okOnClick?.invoke()
                dismiss()
            }
        }
    }
}
