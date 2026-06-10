package com.grl.sms_wa.ui.language

import androidx.activity.viewModels
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityLanguageBinding
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.ui.onboarding.OnboardingActivity
import com.grl.sms_wa.utils.SystemUtil
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.ViewEx.visible
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LanguageActivity : BaseActivity<ActivityLanguageBinding>(ActivityLanguageBinding::inflate) {
    private val viewModel: LanguageViewModel by viewModels()
    private var mLanguageAdapter = LanguageAdapter()

    override fun initView() {
        initUI()
        initListener()
    }

    private fun initListener() {
        binding.apply {
            toolBarLanguage.btnSelect.setOnClickListener {
                mLanguageAdapter.getSelectedModel()?.let { model ->
                    SystemUtil.saveLanguage(this@LanguageActivity, model)
                    if (spManager.isCompletedOnboarding) {
                        startActivityNewTask(MainActivity::class.java)
                    } else {
                        startNextActivity(OnboardingActivity::class.java, isFinish = true)
                    }
                }
            }
        }
    }

    private fun initUI() {
        binding.apply {
            toolBarLanguage.btnBack.setOnClickListener { onBack() }
            toolBarLanguage.btnSelect.visible()
            toolBarLanguage.btnAction.gone()
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