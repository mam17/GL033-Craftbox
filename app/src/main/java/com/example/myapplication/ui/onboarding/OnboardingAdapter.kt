package com.example.myapplication.ui.onboarding

import com.example.myapplication.base.adapter.BaseAdapter
import com.example.myapplication.databinding.ItemOnboardingBinding
import com.example.myapplication.domain.layer.OnboardingModel

class OnboardingAdapter : BaseAdapter<OnboardingModel, ItemOnboardingBinding>(ItemOnboardingBinding::inflate) {
    override fun bind(binding: ItemOnboardingBinding, item: OnboardingModel, position: Int) {
        binding.apply {
            imgBoarding.setImageResource(item.resImage)
            tvTitle.setText(item.resTitle)
            tvOnboarding.setText(item.resDescription)
        }
    }
}