package com.example.myapplication.ui.language

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.domain.usecase.GetListLanguageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(private val getListLanguageUseCase: GetListLanguageUseCase) : ViewModel() {
    val listLanguage = MutableLiveData<List<LanguageModel>>()
    fun loadListLanguage() {
        viewModelScope.launch {
            listLanguage.value = getListLanguageUseCase.execute(GetListLanguageUseCase.Param())
        }
    }
}