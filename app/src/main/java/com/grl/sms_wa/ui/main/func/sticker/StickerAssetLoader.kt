package com.grl.sms_wa.ui.main.func.sticker

import android.content.res.AssetManager
import com.grl.sms_wa.domain.layer.StickerModel
import java.util.Locale

object StickerAssetLoader {
    private const val STICKER_ROOT = "stickers"
    private const val CATEGORY_FOLDER = "category"

    fun loadFromCache(json: String): List<StickerModel> {
        return try {
            val type = object : com.google.gson.reflect.TypeToken<List<com.grl.sms_wa.data.remote.model.StickerPackRemote>>() {}.type
            val remotes: List<com.grl.sms_wa.data.remote.model.StickerPackRemote>? = com.google.gson.Gson().fromJson(json, type)
            remotes?.map { remote ->
                StickerModel(
                    name = remote.name,
                    previewPath = remote.thumbnail,
                    firstImagePath = remote.images.firstOrNull().orEmpty(),
                    detailPaths = remote.images
                )
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun loadStickerCategories(assetManager: AssetManager): List<StickerModel> {
        val categoryPreviews = assetManager.list("$STICKER_ROOT/$CATEGORY_FOLDER")
            .orEmpty()
            .asIterable()
            .filterImageFiles()

        return stickerFolders(assetManager).mapNotNull { folderName ->
            val detailPaths = loadStickerImages(assetManager, folderName)
            val previewFile = findCategoryPreview(folderName, categoryPreviews)
            val previewPath = previewFile?.let { "$STICKER_ROOT/$CATEGORY_FOLDER/$it" }
                ?: detailPaths.firstOrNull()

            previewPath?.let {
                StickerModel(
                    name = folderName.toDisplayName(),
                    previewPath = it,
                    firstImagePath = detailPaths.firstOrNull().orEmpty(),
                    detailPaths = detailPaths
                )
            }
        }
    }

    fun loadYourStickerImages(assetManager: AssetManager): List<String> {
        return stickerFolders(assetManager).flatMap { folderName ->
            loadStickerImages(assetManager, folderName)
        }
    }

    private fun stickerFolders(assetManager: AssetManager): List<String> {
        return assetManager.list(STICKER_ROOT)
            .orEmpty()
            .filterNot { it.equals(CATEGORY_FOLDER, ignoreCase = true) }
            .sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    private fun loadStickerImages(assetManager: AssetManager, folderName: String): List<String> {
        val folderPath = "$STICKER_ROOT/$folderName"
        return assetManager.list(folderPath)
            .orEmpty()
            .asIterable()
            .filterImageFiles()
            .map { "$folderPath/$it" }
    }

    private fun findCategoryPreview(folderName: String, categoryPreviews: List<String>): String? {
        val aliases = previewAliases[folderName.lowercase(Locale.US)]
            .orEmpty() + folderName.lowercase(Locale.US).replace(" ", "_")

        return categoryPreviews.firstOrNull { preview ->
            val normalizedPreview = preview.lowercase(Locale.US)
            aliases.any { alias -> normalizedPreview.contains(alias) }
        }
    }

    private fun Iterable<String>.filterImageFiles(): List<String> {
        return filter { fileName ->
            fileName.endsWith(".png", ignoreCase = true) ||
                fileName.endsWith(".jpg", ignoreCase = true) ||
                fileName.endsWith(".jpeg", ignoreCase = true) ||
                fileName.endsWith(".webp", ignoreCase = true)
        }.sortedWith(String.CASE_INSENSITIVE_ORDER)
    }

    private fun String.toDisplayName(): String {
        return replaceFirstChar { firstChar ->
            if (firstChar.isLowerCase()) {
                firstChar.titlecase(Locale.getDefault())
            } else {
                firstChar.toString()
            }
        }
    }

    private val previewAliases = mapOf(
        "baby girl" to listOf("baby_girl", "babygirl"),
        "bear" to listOf("bear"),
        "dogs" to listOf("dogs", "dog"),
        "duck" to listOf("duck"),
        "inoshuke" to listOf("ino", "inoshuke"),
        "luffy" to listOf("pirates", "luffy"),
        "majin buu" to listOf("ma-bu", "majin", "buu"),
        "octopus" to listOf("octopus"),
        "panda" to listOf("panda"),
        "sprigatito" to listOf("green_fox", "sprigatito")
    )
}
