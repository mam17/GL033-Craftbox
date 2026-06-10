package com.grl.sms_wa.ui.onboarding

import com.grl.sms_wa.base.adapter.BaseAdapter
import com.grl.sms_wa.databinding.ItemOnboardingBinding
import com.grl.sms_wa.domain.layer.OnboardingModel

class OnboardingAdapter : BaseAdapter<OnboardingModel, ItemOnboardingBinding>(ItemOnboardingBinding::inflate) {
    override fun bind(binding: ItemOnboardingBinding, item: OnboardingModel, position: Int) {
        binding.apply {
            imgBoarding.setImageResource(item.resImage)
            tvTitle.setText(item.resTitle)
            tvOnboarding.setText(item.resDescription)
        }
    }
}