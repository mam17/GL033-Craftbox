package com.example.myapplication.ui.uninstall

import android.util.Log
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityAskUninstallBinding
import com.example.myapplication.ui.splash.SplashActivity
import com.example.myapplication.utils.AppEx.goToSystem
import com.example.myapplication.utils.DialogEx.showDialogAlert
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AskUninstallActivity :
    BaseActivity<ActivityAskUninstallBinding>(ActivityAskUninstallBinding::inflate) {

    private var mAskUninstallAdapter = AskUninstallAdapter()
    override fun initView() {
        binding.apply {
            btnBack.setOnClickListener { onBack() }
            rcvItemUninstall.adapter = mAskUninstallAdapter

            btnUninstall.setOnClickListener {
                showDialogAlert(
                    strTitle = getString(R.string.txt_are_you_sure),
                    strBody = getString(R.string.txt_want_uninstall),
                    strCancel = getString(R.string.txt_cancel),
                    strYes = getString(R.string.txt_confirm),
                    okOnClick = {
//                        FirebaseTrackingManager.instance().logEvent(
//                            FirebaseTrackingManager.EVENT_CLICK_UNINSTALL_APP
//                        )
                        Log.i("TAG_AskUninstall", "initView: EVENT_CLICK_UNINSTALL_APP")
                        goToSystem()
                    },
                )
            }
            tvCancel.setOnClickListener {
                startActivityNewTask(SplashActivity::class.java)
            }
        }
    }

    override fun initData() {
        val listAsk = listOf(
            getString(R.string.txt_ask_1),
            getString(R.string.txt_ask_2),
            getString(R.string.txt_ask_3),
            getString(R.string.txt_ask_4),
            getString(R.string.txt_ask_5)
        )

        mAskUninstallAdapter.setData(listAsk)
    }

}