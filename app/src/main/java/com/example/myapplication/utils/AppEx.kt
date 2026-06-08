package com.example.myapplication.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.example.myapplication.R
import com.example.myapplication.ui.splash.SplashActivity
import com.facebook.shimmer.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


object AppEx {
    fun Context.shareText(text: String, title: String = "Share via") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(intent, title))
    }

    fun Context.openAppInStore() {
        val uri = ("market://details?id=" + this.packageName).toUri()
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Log.i("TAGaa", "openAppInStore: ")
        }
    }

    fun Context.shareApp() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_SUBJECT, resources.getString(R.string.app_name))
        intent.putExtra(
            Intent.EXTRA_TEXT,
            "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
        )
        startActivity(Intent.createChooser(intent, resources.getString(R.string.txt_choose_one)))
    }

    fun Context.showPolicyApp() {
        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.data =
            "https://sites.google.com/view/rbx-clothes-maker-skin-editor/home".toUri()
        startActivity(openURL)
    }

    fun Context.goToSystem() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    fun setupAppShortcuts(context: Context) {
        ShortcutManagerCompat.removeAllDynamicShortcuts(context)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutUninstall = ShortcutInfoCompat.Builder(context, "id_uninstall")
            .setShortLabel("Uninstall")
            .setLongLabel("Uninstall")
            .setIcon(IconCompat.createWithResource(context, R.drawable.ic_delete))
            .setIntent(Intent(context, SplashActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                putExtra(Constant.KEY_OPEN_SPLASH, true)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            .build()

        ShortcutManagerCompat.setDynamicShortcuts(
            context,
            listOf(shortcutUninstall)
        )
    }

    fun Context.sendReportByEmail(
        category: String,
        details: String,
        reportedContent: String,
        email: String = Constant.EMAIL_ADDRESS
    ): Boolean {
        val subject = "${getString(R.string.app_name)} - ${getString(R.string.txt_report)}"
        val body = buildString {
            appendLine("Category: $category")
            if (details.isNotBlank()) {
                appendLine()
                appendLine("Details:")
                appendLine(details.trim())
            }
            if (reportedContent.isNotBlank()) {
                appendLine()
                appendLine("Reported content:")
                appendLine(reportedContent.trim())
            }
            appendLine()
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("Android: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})")
            appendLine("App: $packageName")
        }

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = "mailto:".toUri()
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        return try {
            startActivity(Intent.createChooser(intent, getString(R.string.txt_report)))
            true
        } catch (e: ActivityNotFoundException) {
            Log.e("AppEx", "No email app found: ${e.message}")
            false
        }
    }
    fun Context.dpToPx(dp: Int): Int {
        return android.util.TypedValue.applyDimension(
            android.util.TypedValue.COMPLEX_UNIT_DIP,
            dp.toFloat(),
            resources.displayMetrics
        ).toInt()
    }
    fun Long.formatToOrdinalDate(): String {
        val date = Date(this)
        val day = SimpleDateFormat("d", Locale.ENGLISH).format(date).toInt()
        val month = SimpleDateFormat("MMM", Locale.ENGLISH).format(date)
        val year = SimpleDateFormat("yyyy", Locale.ENGLISH).format(date)

        val suffix = when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }

        return "$month $day$suffix, $year"
    }

    fun Long.isSameDay(other: Long): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
        return fmt.format(Date(this)) == fmt.format(Date(other))
    }

    fun String.isImageUri(context: Context): Boolean {
        val uri = toUri()
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) return mimeType.startsWith("image/")
        return startsWith("content://mms/part") ||
            endsWith(".jpg", ignoreCase = true) ||
            endsWith(".jpeg", ignoreCase = true) ||
            endsWith(".png", ignoreCase = true) ||
            endsWith(".webp", ignoreCase = true)
    }

    fun String.isVideoUri(context: Context): Boolean {
        val uri = toUri()
        val mimeType = context.contentResolver.getType(uri)
        if (mimeType != null) return mimeType.startsWith("video/")
        return endsWith(".mp4", ignoreCase = true) ||
            endsWith(".3gp", ignoreCase = true) ||
            endsWith(".mkv", ignoreCase = true) ||
            endsWith(".webm", ignoreCase = true) ||
            endsWith(".mov", ignoreCase = true)
    }


}
