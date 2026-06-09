package com.example.myapplication.ui.main.func.sticker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myapplication.domain.layer.StickerModel
import com.example.myapplication.utils.SpManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class StickerViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val spManager: SpManager
) : ViewModel() {

    private val _allStickers = MutableLiveData<List<StickerModel>>()
    val allStickers: LiveData<List<StickerModel>> get() = _allStickers

    private val _yourStickers = MutableLiveData<List<StickerModel>>()
    val yourStickers: LiveData<List<StickerModel>> get() = _yourStickers

    fun loadStickers() {
        val addedStickerNames = spManager.getAddedStickerNames().toSet()
        val stickers = StickerAssetLoader.loadStickerCategories(context.assets).map { sticker ->
            sticker.copy(isAdded = addedStickerNames.contains(sticker.name))
        }
        _allStickers.value = stickers
        _yourStickers.value = stickers
    }

    fun setStickerAdded(sticker: StickerModel, isAdded: Boolean) {
        spManager.setStickerAdded(sticker.name, isAdded)
        loadStickers()
    }

    fun toggleSticker(sticker: StickerModel) {
        setStickerAdded(sticker, sticker.isAdded.not())
    }
}
