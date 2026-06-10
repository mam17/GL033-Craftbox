package com.grl.sms_wa.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.facebook.shimmer.Shimmer
import com.facebook.shimmer.ShimmerDrawable
import java.io.File
import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Build
import android.provider.MediaStore
import android.os.Environment
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream

fun String.asImageSource(): String {
    return when {
        startsWith("http://") -> this
        startsWith("https://") -> this
        startsWith("content://") -> this
        startsWith("file://") -> this
        File(this).exists() -> this
        else -> "file:///android_asset/$this"
    }
}

object ImageUtils {

    fun ImageView.loadFromPathAction(
        path: String,
        radius: Int = 0,
        isCenterCrop: Boolean = true,
        onLoaded: (() -> Unit)? = null
    ) {
        val context = this.context

        val isAsset = !path.startsWith("http://")
                && !path.startsWith("https://")
                && !path.startsWith("content://")
                && !path.startsWith("file://")
                && !File(path).exists()

        val glideModel: Any = when {
            path.startsWith("content://") -> path.toUri()
            path.startsWith("file://") -> path
            path.startsWith("http") -> path
            File(path).exists() -> File(path)
            else -> path
        }
        val scaleTransformation = if (isCenterCrop) CenterCrop() else FitCenter()
        val transformation = if (radius > 0) {
            MultiTransformation(scaleTransformation, RoundedCorners(radius))
        } else scaleTransformation

        val shimmerDrawable = createShimmerPlaceholder()

        Glide.with(context)
            .load(glideModel)
            .placeholder(shimmerDrawable)
            .apply(RequestOptions().transform(transformation))
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?, model: Any?, target: Target<Drawable?>, isFirstResource: Boolean
                ): Boolean {
                    shimmerDrawable.stopShimmer()
                    onLoaded?.invoke()
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable, model: Any, target: Target<Drawable?>?, dataSource: DataSource, isFirstResource: Boolean
                ): Boolean {
                    shimmerDrawable.stopShimmer()
                    onLoaded?.invoke()
                    return false
                }
            })
            .into(this)
    }

    fun createShimmerPlaceholder(): ShimmerDrawable {
        val shimmer = Shimmer.AlphaHighlightBuilder()
            .setDuration(1000)
            .setBaseAlpha(0.7f)
            .setHighlightAlpha(0.4f)
            .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
            .setAutoStart(true)
            .build()

        val shimmerDrawable = ShimmerDrawable().apply {
            setShimmer(shimmer)
        }
        return shimmerDrawable
    }

    fun setImageToBitmap(context: Context, uri: Uri, onBitmapReady: (Bitmap) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(uri)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    onBitmapReady(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {

                }
            })
    }

    fun setImageFromAsset(
        context: Context,
        assetPath: String,
        onBitmapReady: (Bitmap) -> Unit
    ) {
        Glide.with(context)
            .asBitmap()
            .load("file:///android_asset/$assetPath")
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    onBitmapReady(resource)
                }

                override fun onLoadCleared(placeholder: Drawable?) {}
            })
    }

    fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri? {
        val imagesFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "captured_image_${System.currentTimeMillis()}.jpg")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
            uri = Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return uri
    }

    fun copyUriToInternal(context: Context, uri: Uri): Uri? {
        return try {
            val mediaFolder = File(context.filesDir, "media")
            if (!mediaFolder.exists()) mediaFolder.mkdirs()
            
            val fileName = "media_${System.currentTimeMillis()}_${uri.lastPathSegment ?: "file"}"
            val destFile = File(mediaFolder, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyAssetToInternal(context: Context, assetPath: String): Uri? {
        return try {
            val mediaFolder = File(context.filesDir, "media")
            if (!mediaFolder.exists()) mediaFolder.mkdirs()

            val extension = assetPath.substringAfterLast('.', "png")
            val destFile = File(mediaFolder, "sticker_${System.currentTimeMillis()}.$extension")

            context.assets.open(assetPath).use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun compressImageToInternal(context: Context, uri: Uri, maxSizeKB: Int = 300): Uri? {
        return try {
            val mediaFolder = File(context.filesDir, "media")
            if (!mediaFolder.exists()) mediaFolder.mkdirs()
            val fileName = "comp_${System.currentTimeMillis()}.jpg"
            val destFile = File(mediaFolder, fileName)

            val bitmap = Glide.with(context).asBitmap().load(uri).submit().get()
            var quality = 100
            var stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)

            while (stream.size() > maxSizeKB * 1024 && quality > 10) {
                quality -= 10
                stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
            }

            FileOutputStream(destFile).use { it.write(stream.toByteArray()) }
            Uri.fromFile(destFile)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun saveImageToGallery(context: Context, path: String, onResult: (Boolean) -> Unit) {
        try {
            val uri = if (path.startsWith("content://")) Uri.parse(path) else Uri.parse("file://$path")
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            if (bitmap == null) {
                onResult(false)
                return
            }

            val filename = "SMSColor_${System.currentTimeMillis()}.jpg"
            var fos: OutputStream? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                context.contentResolver.let { resolver ->
                    val imageUri =
                        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    fos = imageUri?.let { resolver.openOutputStream(it) }
                }
            } else {
                val imagesDir =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val image = File(imagesDir, filename)
                fos = FileOutputStream(image)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                onResult(true)
            } ?: onResult(false)

        } catch (e: Exception) {
            e.printStackTrace()
            onResult(false)
        }
    }
}
