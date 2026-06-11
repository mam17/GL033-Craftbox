package com.grl.sms_wa.ui.language

import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityLanguageBinding
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.ui.onboarding.OnboardingActivity
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.SystemUtil
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.ViewEx.textColorRes
import com.grl.sms_wa.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()
    private var fromSplash = false

    override fun initView() {
        fromSplash = intent.getBooleanExtra(Constant.KEY_FROM_SPLASH, false)
        Log.i("TAG_LANGUAGE", "initUI: fromSplash $fromSplash")

        initUI()
        initListener()
    }

    private fun initListener() {
        binding.apply {
            toolBarLanguage.btnSelect.setOnClickListener {
                mLanguageAdapter.getSelectedModel()?.let { model ->
                    SystemUtil.saveLanguage(this@LanguageActivity, model)
                    if (fromSplash) {
                        startNextActivity(OnboardingActivity::class.java, isFinish = true)
                    } else {
                        startActivityNewTask(MainActivity::class.java)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.isVisible = !fromSplash
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnSelect.visible()
            toolBarLanguage.btnSelect.textColorRes(R.color.col_main)
            toolBarLanguage.btnAction.gone()
            toolBarLanguage.tvTitle.text = getString(R.string.txt_language)
            rclLanguage.adapter = mLanguageAdapter
            mLanguageAdapter.setOnItemClick { _, position ->
                mLanguageAdapter.selectItem(position)
            }
        }
    }

    override fun initData() {
        viewModel.loadListLanguage()
    }

    override fun initObserver() {
        viewModel.listLanguage.observe(this) { list ->
            val currentCode = SystemUtil.getLanguage(this)
            list.forEach { model ->
                model.selected = model.languageCode == currentCode
            }
            mLanguageAdapter.setData(list)
        }
    }


}