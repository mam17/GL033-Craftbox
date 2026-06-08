package com.example.myapplication.ui.main.func.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ThemesViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : ViewModel() {
}