package com.example.myapplication.utils

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.role.RoleManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.provider.Telephony
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri

object PermissionUtils {
    fun isNotificationPermissionGranted(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestNotificationPermission(
        context: Context,
        launcher: ActivityResultLauncher<String>
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !isNotificationPermissionGranted(context)
        ) {
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }


    fun isDefaultSmsApp(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            val isHeld = roleManager?.isRoleHeld(RoleManager.ROLE_SMS) == true
            Log.i("PermissionUtil", "isDefaultSmsApp (Q+): isHeld=$isHeld")
            return isHeld
        } else {
            val defaultPackage = Telephony.Sms.getDefaultSmsPackage(context)
            Log.i(
                "PermissionUtil",
                "isDefaultSmsApp (Pre-Q): defaultPkg=$defaultPackage, appPkg=${context.packageName}"
            )
            return defaultPackage == context.packageName
        }
    }

    fun getRequestDefaultSmsIntent(context: Context): Intent? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_SMS) &&
                !roleManager.isRoleHeld(RoleManager.ROLE_SMS)
            ) {
                roleManager.createRequestRoleIntent(RoleManager.ROLE_SMS)
            } else {
                null
            }
        } else {
            if (Telephony.Sms.getDefaultSmsPackage(context) != context.packageName) {
                Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT).apply {
                    putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.packageName)
                }
            } else {
                null
            }
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        val isRuntimePermissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        return isRuntimePermissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun hasFullScreenIntentPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 34) {
            val nm = context.getSystemService(NotificationManager::class.java)
            nm?.canUseFullScreenIntent() == true
        } else true
    }

    fun isExactAlarmGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.canScheduleExactAlarms()
        } else true
    }

    fun requestExactAlarmPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
        }
    }

    fun hasAllPermissions(context: Context): Boolean {
        return hasNotificationPermission(context)
                && hasFullScreenIntentPermission(context)
                && isExactAlarmGranted(context)
    }

    fun openFullScreenIntentSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= 34) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = "package:${context.packageName}".toUri()
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun hasSmsPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
