package com.example.myapplication.ui.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.layer.OnboardingModel
import com.example.myapplication.domain.usecase.GetListOnboardingUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(private val getListOnBoardingUseCase: GetListOnboardingUseCase) :
    ViewModel() {
    val listOnBoarding = MutableLiveData<List<OnboardingModel>>()
    fun loadListOnBoarding() {
        viewModelScope.launch {
            listOnBoarding.value =
                getListOnBoardingUseCase.execute(GetListOnboardingUseCase.Param())
        }
    }
}