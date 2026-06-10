package com.grl.sms_wa.ui.dialog

import android.app.Activity
import android.util.Log
import com.grl.sms_wa.base.dialog.BaseDialog
import com.grl.sms_wa.databinding.DialogRateBinding

class DialogRate(
    private val activity: Activity,
    private val onRating: () -> Unit,
    private val onQuit: () -> Unit
) : BaseDialog<DialogRateBinding>(activity, DialogRateBinding::inflate) {
    override fun initView() {
        super.initView()
        binding.apply {
            btnSubmit.setOnClickListener {
                ratingBar.rating.let { rating ->
                    Log.d("Rating", "Rating value: $rating")
                    when {
                        rating == 0f -> dismiss()
                        rating <= 3.0 -> onQuit.invoke()
                        else -> onRating.invoke()
                    }
                    dismiss()
                }
            }
            btnNotNow.setOnClickListener { dismiss() }
        }
    }
}