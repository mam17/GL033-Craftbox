package com.example.myapplication.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivitySplashBinding
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.ui.uninstall.UninstallActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.NetworkUtil
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            goToNextAction()
        }
    private var isCheckUninstall = false
    private var isStartingNextScreen = false
    private var isNetworkDialogShowing = false
    override fun initView() {
        isCheckUninstall = intent.getBooleanExtra(Constant.KEY_OPEN_SPLASH, false)
        trackNotificationOpen(intent)
        if (PermissionUtils.isNotificationPermissionGranted(this)) {
            goToNextAction()
        } else {
            PermissionUtils.requestNotificationPermission(this, notificationPermissionLauncher)
        }
    }

    override fun initData() {

    }

    private fun checkVersion() {
//        if (remoteConfig.enableForceUpdate) {
//            val currentAppVersion = BuildConfig.VERSION_NAME
//            Log.i("checkVersion", "Current app version: $currentAppVersion")
//            Log.i("checkVersion", "Remote config version: ${remoteConfig.newVersionRemote}")
//            if (currentAppVersion != remoteConfig.newVersionRemote) {
//                showDialogForceUpdate(onClick = {
//                    openAppInStore()
//                })
//            } else {
//                startSplashFlowOnce()
//            }
//        } else {
//            Log.i("checkVersion", "checkVersion: is check = false")
//            startSplashFlowOnce()
//        }
    }

    private fun trackNotificationOpen(intent: Intent?) {
        if (intent?.getBooleanExtra(
                NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION,
                false
            ) != true
        ) {
            return
        }

        Log.i("TAG_SPLASH", "trackNotificationOpen: EVENT_NOTIFICATION_CLICK_OPEN_APP")
//        FirebaseTrackingManager.instance().logEvent(
//            FirebaseTrackingManager.EVENT_NOTIFICATION_CLICK_OPEN_APP
//        )
        intent.removeExtra(NotificationUtils.EXTRA_OPEN_FROM_NOTIFICATION)
    }

    fun goToNextAction() {
        if (isStartingNextScreen) return
        if (!NetworkUtil.isNetworkAvailable(this)) {
            showNetworkErrorDialog()
            return
        }
        isStartingNextScreen = true
        lifecycleScope.launch {
            val startedAt = System.currentTimeMillis()
            val remainingDelay = SPLASH_MIN_DURATION_MS - (System.currentTimeMillis() - startedAt)
            if (remainingDelay > 0) delay(remainingDelay)
            openNextScreen()
        }
    }

    private fun openNextScreen() {
        if (isCheckUninstall) {
            startNextActivity(UninstallActivity::class.java, isFinish = true)
        } else {
            if (spManager.isCompletedOnboarding) {
                startActivityNewTask(MainActivity::class.java)
            } else {
                val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
                startNextActivity(LanguageActivity::class.java, bundle, isFinish = true)
            }
        }
    }

    private fun showNetworkErrorDialog() {
        if (isNetworkDialogShowing) return
        isNetworkDialogShowing = true
        showDialogAlert(
            strTitle = getString(R.string.txt_network_error),
            strBody = getString(R.string.txt_network_error_message),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_try_again),
            okOnClick = {
                isNetworkDialogShowing = false
                goToNextAction()
            },
            cancelOnClick = {
                isNetworkDialogShowing = false
                finish()
            }
        )
    }

    companion object {
        private const val TAG = "TAG_SPLASH"
        private const val SPLASH_MIN_DURATION_MS = 1_500L
        private const val REMOTE_CONFIG_TIMEOUT_MS = 4_000L
    }
}