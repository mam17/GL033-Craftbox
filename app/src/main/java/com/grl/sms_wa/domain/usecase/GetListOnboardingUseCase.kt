package com.grl.sms_wa.domain.usecase

import com.grl.sms_wa.R
import com.grl.sms_wa.domain.layer.OnboardingModel
import javax.inject.Inject

class GetListOnboardingUseCase @Inject constructor() :
    UseCase<GetListOnboardingUseCase.Param, List<OnboardingModel>>() {

    open class Param() : UseCase.Param()

    override suspend fun execute(param: Param): List<OnboardingModel> = listOf(
        OnboardingModel(R.drawable.img_onb1, R.string.txt_title_onb1,R.string.txt_desc_onb1),
        OnboardingModel(R.drawable.img_onb2, R.string.txt_title_onb2,R.string.txt_desc_onb2),
        OnboardingModel(R.drawable.img_onb3, R.string.txt_title_onb3,R.string.txt_desc_onb3),
    )
}