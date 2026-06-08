package com.example.myapplication.ui.components.preview.activity

import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMediaPreviewBinding
import com.example.myapplication.utils.AppEx.isImageUri
import com.example.myapplication.utils.AppEx.isVideoUri
import com.example.myapplication.utils.ImageUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@AndroidEntryPoint
class MediaPreviewActivity :
    BaseActivity<ActivityMediaPreviewBinding>(ActivityMediaPreviewBinding::inflate) {

    private val mediaPath: String by lazy {
        intent.getStringExtra(EXTRA_MEDIA_PATH).orEmpty()
    }
    private val senderName: String by lazy {
        intent.getStringExtra(EXTRA_SENDER_NAME).orEmpty()
    }
    private val sentTime: Long by lazy {
        intent.getLongExtra(EXTRA_SENT_TIME, 0L)
    }
    private val isImage: Boolean by lazy { mediaPath.isImageUri(this) }
    private val isVideo: Boolean by lazy { mediaPath.isVideoUri(this) }

    override fun initView() {
        binding.btnBack.setOnClickListener {
            onBack()
        }

        binding.ivPlay.setOnClickListener {
            playVideo()
        }
        binding.btnDownloadMedia.setOnClickListener {
            downloadMedia()
        }
    }

    override fun initData() {
        if (mediaPath.isBlank()) {
            finish()
            return
        }
        bindPreview()
    }

    private fun bindPreview() {
        binding.tvName.text = senderName.ifBlank { getString(R.string.txt_unknown_sender) }
        binding.tvTime.text = sentTime.formatPreviewTime()
        binding.ivPreview.isVisible = isImage
        binding.videoPreview.isVisible = isVideo
        binding.ivPlay.isVisible = isVideo

        if (isImage) {
            with(ImageUtils) {
                binding.ivPreview.loadFromPathAction(
                    path = mediaPath,
                    isCenterCrop = false
                )
            }
            return
        }

        if (isVideo) {
            binding.videoPreview.setVideoURI(mediaPath.toPreviewUri())
            binding.videoPreview.setOnCompletionListener {
                binding.ivPlay.isVisible = true
            }
        }
    }

    private fun playVideo() {
        if (!isVideo) return
        binding.ivPlay.isVisible = false
        binding.videoPreview.start()
    }

    private fun downloadMedia() {
        val success = saveMediaToGallery()
        val message = if (success) {
            getString(R.string.txt_downloaded)
        } else {
            getString(R.string.txt_download_failed)
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun saveMediaToGallery(): Boolean {
        return try {
            val sourceUri = mediaPath.toPreviewUri()
            val mimeType = contentResolver.getType(sourceUri) ?: mediaPath.fallbackMimeType()
            val extension = mimeType.substringAfter("/", "file")
            val fileName = "ColorSMS_${System.currentTimeMillis()}.$extension"
            val mediaUri = createOutputMediaUri(fileName, mimeType) ?: return false

            openInputStream(sourceUri)?.use { input ->
                mediaUri.openOutputStreamCompat()?.use { output ->
                    input.copyTo(output)
                }
            } ?: return false

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentResolver.update(
                    mediaUri,
                    ContentValues().apply {
                        put(MediaStore.MediaColumns.IS_PENDING, 0)
                    },
                    null,
                    null
                )
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun createOutputMediaUri(fileName: String, mimeType: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val collection = when {
                mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                else -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
            }
            val relativePath = when {
                mimeType.startsWith("video/") -> Environment.DIRECTORY_MOVIES
                mimeType.startsWith("image/") -> Environment.DIRECTORY_PICTURES
                else -> Environment.DIRECTORY_DOWNLOADS
            }
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath)
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
            contentResolver.insert(collection, values)
        } else {
            @Suppress("DEPRECATION")
            val directory = when {
                mimeType.startsWith("video/") -> Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_MOVIES
                )

                mimeType.startsWith("image/") -> Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                )

                else -> Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
            }
            directory.mkdirs()
            Uri.fromFile(File(directory, fileName))
        }
    }

    private fun openInputStream(uri: Uri) = when (uri.scheme) {
        "file" -> FileInputStream(File(uri.path.orEmpty()))
        else -> contentResolver.openInputStream(uri)
    }

    private fun Uri.openOutputStreamCompat() = when (scheme) {
        "file" -> FileOutputStream(File(path.orEmpty()))
        else -> contentResolver.openOutputStream(this)
    }

    private fun String.toPreviewUri(): Uri {
        return when {
            startsWith("content://") -> toUri()
            startsWith("file://") -> toUri()
            File(this).exists() -> Uri.fromFile(File(this))
            else -> toUri()
        }
    }

    private fun String.fallbackMimeType(): String {
        return when {
            isVideoUri(this@MediaPreviewActivity) -> "video/mp4"
            isImageUri(this@MediaPreviewActivity) -> "image/jpeg"
            else -> "application/octet-stream"
        }
    }

    private fun Long.formatPreviewTime(): String {
        if (this <= 0L) return ""
        return SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault()).format(Date(this))
    }

    companion object {
        const val EXTRA_MEDIA_PATH = "extra_media_path"
        const val EXTRA_SENDER_NAME = "extra_sender_name"
        const val EXTRA_SENT_TIME = "extra_sent_time"
    }
}
