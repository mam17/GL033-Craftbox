package com.grl.sms_wa.utils

import android.app.Activity
import com.grl.sms_wa.ui.dialog.DialogAlert

object DialogEx {
    fun Activity.showDialogAlert(
        strTitle: String? = null,
        strBody: String? = null,
        strCancel: String? = null,
        strYes: String? = null,
        okOnClick: () -> Unit,
        cancelOnClick: (() -> Unit)? =null,
    ) {
        val dialog = DialogAlert(this, strTitle, strBody, strCancel, strYes)
        dialog.show()
        dialog.okOnClick = {
            okOnClick.invoke()
        }
        dialog.cancelOnClick = {
            cancelOnClick?.invoke()
        }
    }
}