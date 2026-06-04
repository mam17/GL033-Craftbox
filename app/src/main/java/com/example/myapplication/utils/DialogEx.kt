package com.example.myapplication.utils

import android.app.Activity
import com.example.myapplication.ui.dialog.DialogAlert

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