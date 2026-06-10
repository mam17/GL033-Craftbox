package com.grl.sms_wa.ui.main.func.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grl.sms_wa.domain.layer.CategoryThemeModel
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.domain.usecase.GetListThemeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ThemesViewModel @Inject constructor(
    private val getListThemeUseCase: GetListThemeUseCase
) : ViewModel() {

    private val _categoriesState = MutableStateFlow<List<CategoryThemeModel>>(emptyList())
    val categoriesState: StateFlow<List<CategoryThemeModel>> = _categoriesState.asStateFlow()

    private val _selectedCategoryIndex = MutableStateFlow(0)
    val selectedCategoryIndex: StateFlow<Int> = _selectedCategoryIndex.asStateFlow()

    private val _themesOfSelectedCategory = MutableStateFlow<List<ThemeMessModel>>(emptyList())
    val themesOfSelectedCategory: StateFlow<List<ThemeMessModel>> =
        _themesOfSelectedCategory.asStateFlow()

    init {
        fetchThemes()
    }

    private fun fetchThemes() {
        viewModelScope.launch {
            val categories = getListThemeUseCase.execute(GetListThemeUseCase.Param())
            _categoriesState.value = categories
            if (categories.isNotEmpty()) {
                selectCategory(0)
            }
        }
    }

    fun selectCategory(index: Int) {
        val categories = _categoriesState.value
        if (index in categories.indices) {
            _selectedCategoryIndex.value = index
            _themesOfSelectedCategory.value = categories[index].listTheme
        }
    }
}
