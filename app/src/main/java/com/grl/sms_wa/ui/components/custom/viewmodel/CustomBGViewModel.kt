package com.grl.sms_wa.ui.components.custom.viewmodel

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random
import javax.inject.Inject

sealed interface CustomBackgroundSelection {
    data object Unchanged : CustomBackgroundSelection
    data object Cleared : CustomBackgroundSelection
    data class Asset(val path: String) : CustomBackgroundSelection
    data class Gallery(val uri: String) : CustomBackgroundSelection
    data class SolidColor(val color: Int) : CustomBackgroundSelection
    data class Gradient(val gradient: BackgroundGradient) : CustomBackgroundSelection
}

enum class GradientDirection {
    HORIZONTAL,
    VERTICAL
}

data class BackgroundGradient(
    val colors: List<Int>,
    val direction: GradientDirection = GradientDirection.HORIZONTAL
) {
    init {
        require(colors.size in MIN_GRADIENT_COLORS..MAX_GRADIENT_COLORS)
    }

    private companion object {
        const val MIN_GRADIENT_COLORS = 2
        const val MAX_GRADIENT_COLORS = 3
    }
}

@HiltViewModel
class CustomBGViewModel @Inject constructor() : ViewModel() {

    val presetColors: List<Int> = List(PRESET_COLOR_COUNT) {
        randomColor()
    }

    val presetGradients: List<BackgroundGradient> = List(PRESET_GRADIENT_COUNT) {
        BackgroundGradient(List(MAX_GRADIENT_COLORS) { randomColor() })
    }

    private val _gradientDirection = MutableStateFlow(GradientDirection.HORIZONTAL)
    val gradientDirection: StateFlow<GradientDirection> = _gradientDirection.asStateFlow()

    private val _backgroundSelection =
        MutableStateFlow<CustomBackgroundSelection>(CustomBackgroundSelection.Unchanged)
    val backgroundSelection: StateFlow<CustomBackgroundSelection> =
        _backgroundSelection.asStateFlow()

    private val _isPictureBackgroundVisible = MutableStateFlow(false)
    val isPictureBackgroundVisible: StateFlow<Boolean> =
        _isPictureBackgroundVisible.asStateFlow()

    fun showPictureBackgrounds() {
        _isPictureBackgroundVisible.value = true
    }

    fun hidePictureBackgrounds() {
        _isPictureBackgroundVisible.value = false
    }

    fun selectAssetBackground(assetPath: String) {
        _backgroundSelection.value = CustomBackgroundSelection.Asset(assetPath)
        hidePictureBackgrounds()
    }

    fun selectGalleryBackground(uri: String) {
        _backgroundSelection.value = CustomBackgroundSelection.Gallery(uri)
        hidePictureBackgrounds()
    }

    fun selectSolidColor(color: Int) {
        _backgroundSelection.value = CustomBackgroundSelection.SolidColor(color)
    }

    fun selectGradient(colors: List<Int>) {
        _backgroundSelection.value =
            CustomBackgroundSelection.Gradient(
                BackgroundGradient(colors, _gradientDirection.value)
            )
    }

    fun toggleGradientDirection() {
        val direction = when (_gradientDirection.value) {
            GradientDirection.HORIZONTAL -> GradientDirection.VERTICAL
            GradientDirection.VERTICAL -> GradientDirection.HORIZONTAL
        }
        _gradientDirection.value = direction
        val currentSelection = _backgroundSelection.value
        if (currentSelection is CustomBackgroundSelection.Gradient) {
            _backgroundSelection.value = CustomBackgroundSelection.Gradient(
                currentSelection.gradient.copy(direction = direction)
            )
        }
    }

    fun clearBackground() {
        _backgroundSelection.value = CustomBackgroundSelection.Cleared
    }

    private fun randomColor(): Int = android.graphics.Color.rgb(
        Random.nextInt(MIN_COLOR_CHANNEL, MAX_COLOR_CHANNEL),
        Random.nextInt(MIN_COLOR_CHANNEL, MAX_COLOR_CHANNEL),
        Random.nextInt(MIN_COLOR_CHANNEL, MAX_COLOR_CHANNEL)
    )

    private companion object {
        const val PRESET_COLOR_COUNT = 10
        const val PRESET_GRADIENT_COUNT = 10
        const val MAX_GRADIENT_COLORS = 3
        const val MIN_COLOR_CHANNEL = 40
        const val MAX_COLOR_CHANNEL = 216
    }
}
