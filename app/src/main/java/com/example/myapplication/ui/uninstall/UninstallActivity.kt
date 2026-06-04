package com.example.myapplication.ui.uninstall

import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityUninstallBinding
import com.example.myapplication.ui.main.MainActivity
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
        startNextActivity(MainActivity::class.java, isFinish = true)
    }
}