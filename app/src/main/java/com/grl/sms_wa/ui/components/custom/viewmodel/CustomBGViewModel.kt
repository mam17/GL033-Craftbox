package com.grl.sms_wa.ui.components.custom.viewmodel

import androidx.lifecycle.ViewModel
import com.grl.sms_wa.utils.SpManager
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
class CustomBGViewModel @Inject constructor(
    private val spManager: SpManager
) : ViewModel() {

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

    private val _overlayAlpha = MutableStateFlow(0)
    val overlayAlpha: StateFlow<Int> = _overlayAlpha.asStateFlow()

    private val _blurRadius = MutableStateFlow(0)
    val blurRadius: StateFlow<Int> = _blurRadius.asStateFlow()

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

    fun loadCurrentTheme() {
        val currentTheme = spManager.getCurrentTheme() ?: return
        _overlayAlpha.value = currentTheme.overlayAlpha
        _blurRadius.value = currentTheme.blurRadius
        _gradientDirection.value = if (currentTheme.isOrientationColorBG) {
            GradientDirection.HORIZONTAL
        } else {
            GradientDirection.VERTICAL
        }
        
        // Cập nhật backgroundSelection dựa trên pathBG
        val path = currentTheme.pathBG
        if (path.isNotEmpty()) {
            _backgroundSelection.value = when {
                path.startsWith("#") -> CustomBackgroundSelection.SolidColor(android.graphics.Color.parseColor(path))
                path.startsWith("content://") || path.startsWith("file://") -> CustomBackgroundSelection.Gallery(path)
                else -> CustomBackgroundSelection.Asset(path)
            }
        }
    }

    fun setOverlayAlpha(alpha: Int) {
        _overlayAlpha.value = alpha.coerceIn(0, 255)
    }

    fun setBlurRadius(radius: Int) {
        _blurRadius.value = radius.coerceIn(0, 25)
    }

    fun clearBackground() {
        _backgroundSelection.value = CustomBackgroundSelection.Cleared
        _overlayAlpha.value = 0
        _blurRadius.value = 0
    }

    /**
     * Lưu lựa chọn background vào [ThemeMessModel] hiện tại qua [SpManager.saveCurrentTheme].
     * - [ThemeMessModel.pathBG]: path asset hoặc gallery URI (rỗng nếu không phải ảnh).
     * - [ThemeMessModel.isOrientationColorBG]: false = vertical, true = horizontal.
     * - [ThemeMessModel.overlayAlpha]: alpha của lớp tối phủ (0–255).
     * - [ThemeMessModel.blurRadius]: bán kính blur (0–25).
     */
    fun saveBackground() {
        val currentTheme = spManager.getCurrentTheme() ?: return
        val selection = _backgroundSelection.value
        val newPath = when (selection) {
            is CustomBackgroundSelection.Asset -> selection.path
            is CustomBackgroundSelection.Gallery -> selection.uri
            else -> ""
        }
        spManager.saveCurrentTheme(
            currentTheme.copy(
                pathBG = newPath,
                isOrientationColorBG = _gradientDirection.value == GradientDirection.HORIZONTAL,
                overlayAlpha = _overlayAlpha.value,
                blurRadius = _blurRadius.value
            )
        )
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
