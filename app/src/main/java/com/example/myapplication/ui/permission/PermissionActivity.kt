package com.example.myapplication.ui.permission

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityPermissionBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.ViewEx.applyThemeFont
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
            strBody = getString(com.example.myapplication.R.string.txt_set_as_default_sms_required),
            strCancel = getString(com.example.myapplication.R.string.txt_cancel),
            strYes = getString(com.example.myapplication.R.string.txt_ok),
            okOnClick = {
                requestDefaultSmsRole()
            }
        )
    }

    private fun goToMain() {
        startActivityNewTask(MainActivity::class.java)
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        val mainColor = theme.colMain.toColorInt()
        binding.btnOk.backgroundTintList = android.content.res.ColorStateList.valueOf(mainColor)
        binding.root.applyThemeFont(theme.font)
    }
}
