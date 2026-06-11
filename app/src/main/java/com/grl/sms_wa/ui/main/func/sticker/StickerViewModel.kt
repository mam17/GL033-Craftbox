package com.grl.sms_wa.ui.main.func.sticker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.grl.sms_wa.domain.layer.StickerModel
import com.grl.sms_wa.utils.SpManager
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
        val cachedCatalogJson = spManager.getStickerCatalog()
        android.util.Log.d("StickerViewModel", "cachedCatalogJson is null or empty: ${cachedCatalogJson.isNullOrEmpty()}")
        val rawStickers = if (!cachedCatalogJson.isNullOrEmpty()) {
            android.util.Log.d("StickerViewModel", "cachedCatalogJson: $cachedCatalogJson")
            val cached = StickerAssetLoader.loadFromCache(cachedCatalogJson)
            android.util.Log.d("StickerViewModel", "Loaded from cache: ${cached.size} stickers")
            if (cached.isEmpty()) {
                android.util.Log.d("StickerViewModel", "Cache was empty or invalid, fallback to local assets")
                StickerAssetLoader.loadStickerCategories(context.assets)
            } else {
                cached
            }
        } else {
            android.util.Log.d("StickerViewModel", "No cache, load from local assets")
            StickerAssetLoader.loadStickerCategories(context.assets)
        }
        val stickers = rawStickers.map { sticker ->
            sticker.copy(isAdded = addedStickerNames.contains(sticker.name))
        }
        _allStickers.value = stickers
        _yourStickers.value = stickers.filter { it.isAdded }
    }

    fun setStickerAdded(sticker: StickerModel, isAdded: Boolean) {
        spManager.setStickerAdded(sticker.name, isAdded)
        loadStickers()
    }

    fun toggleSticker(sticker: StickerModel) {
        setStickerAdded(sticker, sticker.isAdded.not())
    }
}
