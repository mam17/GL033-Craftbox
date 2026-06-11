package com.grl.sms_wa.ui.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityPermissionBinding
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.utils.DialogEx.showDialogAlert
import com.grl.sms_wa.utils.PermissionUtils
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PermissionActivity :
    BaseActivity<ActivityPermissionBinding>(ActivityPermissionBinding::inflate) {
    private val requestDefaultSmsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (PermissionUtils.isDefaultSmsApp(this)) {
                goToMain()
            } else {
                showDefaultSmsRequiredDialog()
            }
        }

    override fun initView() {
        binding.btnOk.setOnClickListener {
            requestDefaultSmsRole()
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
        if (PermissionUtils.isDefaultSmsApp(this)) {
            goToMain()
        }
    }

    private fun requestDefaultSmsRole() {
        if (PermissionUtils.isDefaultSmsApp(this)) {
            goToMain()
            return
        }

        val requestIntent = PermissionUtils.getRequestDefaultSmsIntent(this)
        if (requestIntent != null) {
            requestDefaultSmsLauncher.launch(requestIntent)
        } else {
            showDefaultSmsRequiredDialog()
        }
    }

    private fun showDefaultSmsRequiredDialog() {
        showDialogAlert(
            strBody = getString(com.grl.sms_wa.R.string.txt_set_as_default_sms_required),
            strCancel = getString(com.grl.sms_wa.R.string.txt_cancel),
            strYes = getString(com.grl.sms_wa.R.string.txt_ok),
            okOnClick = {
                requestDefaultSmsRole()
            }
        )
    }

    private fun goToMain() {
        startActivityNewTask(MainActivity::class.java, )
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        val mainColor = theme.colMain.toColorInt()
        binding.btnOk.backgroundTintList = android.content.res.ColorStateList.valueOf(mainColor)
        binding.root.applyThemeFont(theme.font)
    }
}
