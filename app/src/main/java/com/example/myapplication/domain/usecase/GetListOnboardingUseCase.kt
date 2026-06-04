package com.example.myapplication.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.domain.layer.OnboardingModel
import javax.inject.Inject

class GetListOnboardingUseCase @Inject constructor() :
    UseCase<GetListOnboardingUseCase.Param, List<OnboardingModel>>() {

    open class Param() : UseCase.Param()

    override suspend fun execute(param: Param): List<OnboardingModel> = listOf(
        OnboardingModel(R.mipmap.ic_launcher, R.string.txt_title_onb1,R.string.txt_desc_onb1),
        OnboardingModel(R.mipmap.ic_launcher, R.string.txt_title_onb2,R.string.txt_desc_onb2),
        OnboardingModel(R.mipmap.ic_launcher, R.string.txt_title_onb3,R.string.txt_desc_onb3),
    )
}