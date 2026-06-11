package com.grl.sms_wa.ui.components.custom.viewmodel

import androidx.lifecycle.ViewModel
import com.grl.sms_wa.utils.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CustomBubbleViewModel @Inject constructor(
    private val spManager: SpManager
) : ViewModel() {

}