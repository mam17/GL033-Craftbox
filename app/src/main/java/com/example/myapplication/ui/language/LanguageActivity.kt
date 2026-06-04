package com.example.myapplication.ui.language

import androidx.activity.viewModels
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityLanguageBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.onboarding.OnboardingActivity
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.ViewEx.visible
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