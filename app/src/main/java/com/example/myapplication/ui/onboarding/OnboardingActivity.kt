package com.example.myapplication.ui.onboarding

import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityOnboardingBinding
import com.example.myapplication.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardingActivity :
    BaseActivity<ActivityOnboardingBinding>(ActivityOnboardingBinding::inflate) {
    private val viewModel: OnboardingViewModel by viewModels()
    private var mAdapter = OnboardingAdapter()
    private var currentPosition = 0

    override fun initView() {
        binding.apply {
            vpOnBoarding.adapter = mAdapter
            dotIndicator.attachTo(vpOnBoarding)

            vpOnBoarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPosition = position
                    if (position == mAdapter.itemCount - 1) {
                        btnNext.setText(R.string.txt_get_start)
                    } else {
                        btnNext.setText(R.string.txt_next)
                    }
                }
            })

            btnNext.setOnClickListener {
                if (currentPosition < mAdapter.itemCount - 1) {
                    vpOnBoarding.currentItem = currentPosition + 1
                } else {
                    startActivityNewTask(MainActivity::class.java)
                }
            }
        }
    }

    override fun initData() {
        viewModel.loadListOnBoarding()
    }

    override fun initObserver() {
        viewModel.listOnBoarding.observe(this) { list ->
            mAdapter.setData(ArrayList(list))
        }
    }

}