package com.grl.sms_wa.ui.uninstall

import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityUninstallBinding
import com.grl.sms_wa.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UninstallActivity :
    BaseActivity<ActivityUninstallBinding>(ActivityUninstallBinding::inflate) {

    override fun initView() {
        setupListener()
    }

    override fun initData() {
    }

    private fun setupListener() {
        binding.apply {
            btnBack.setOnClickListener { finishAffinity() }

            btnExplore1.setOnClickListener { nextAction() }
            btnExplore2.setOnClickListener { nextAction() }

            tvStill.setOnClickListener { startNextActivity(AskUninstallActivity::class.java) }
            btnDontUninstall.setOnClickListener { nextAction() }
        }
    }

    fun nextAction() {
        startActivityNewTask(MainActivity::class.java)
    }
}
