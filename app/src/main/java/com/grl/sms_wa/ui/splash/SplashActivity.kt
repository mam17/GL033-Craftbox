package com.grl.sms_wa.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.data.remote.AssetCatalogRepository
import com.grl.sms_wa.databinding.ActivitySplashBinding
import com.grl.sms_wa.ui.permission.PermissionActivity
import com.grl.sms_wa.ui.language.LanguageActivity
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.ui.components.mess.activity.MessengerActivity
import com.grl.sms_wa.ui.uninstall.UninstallActivity
import com.grl.sms_wa.utils.Constant
import com.grl.sms_wa.utils.DialogEx.showDialogAlert
import com.grl.sms_wa.utils.NetworkUtil
import com.grl.sms_wa.utils.PermissionUtils
import com.grl.sms_wa.utils.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.milliseconds

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : BaseActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {
    @Inject
    lateinit var assetCatalogRepository: AssetCatalogRepository

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            goToNextAction()
        }
    private var isCheckUninstall = false
    private var isStartingNextScreen = false
    private var isNetworkDialogShowing = false
    private val notificationSmsAddress: String
        get() = intent.getStringExtra(Constant.EXTRA_SMS_NOTIFICATION_ADDRESS).orEmpty()
    private val notificationSmsBody: String
        get() = intent.getStringExtra(Constant.EXTRA_SMS_NOTIFICATION_BODY).orEmpty()

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
        if (notificationSmsAddress.isBlank() && !NetworkUtil.isNetworkAvailable(this)) {
            showNetworkErrorDialog()
            return
        }
        isStartingNextScreen = true
        lifecycleScope.launch {
            val startedAt = System.currentTimeMillis()
            if (NetworkUtil.isNetworkAvailable(this@SplashActivity) && !assetCatalogRepository.isCatalogCached()) {
                try {
                    withTimeoutOrNull(REMOTE_CONFIG_TIMEOUT_MS.milliseconds) {
                        assetCatalogRepository.fetchAndCacheIfNeeded()
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to fetch remote catalog", e)
                }
            }
            val remainingDelay = SPLASH_MIN_DURATION_MS - (System.currentTimeMillis() - startedAt)
            if (remainingDelay > 0) delay(remainingDelay.milliseconds)
            openNextScreen()
        }
    }

    private fun openNextScreen() {
        if (notificationSmsAddress.isNotBlank()) {
            openMessengerFromNotification()
            return
        }

        if (isCheckUninstall) {
            startNextActivity(UninstallActivity::class.java, isFinish = true)
        } else {
            if (spManager.isCompletedOnboarding) {
                if (PermissionUtils.isDefaultSmsApp(this)) {
                    startActivityNewTask(MainActivity::class.java)
                } else {
                    startActivityNewTask(PermissionActivity::class.java)
                }
            } else {
                val bundle = Bundle().apply { putBoolean(Constant.KEY_FROM_SPLASH, true) }
                startNextActivity(LanguageActivity::class.java, bundle, isFinish = true)
            }
        }
    }

    private fun openMessengerFromNotification() {
        startActivity(
            Intent(this, MessengerActivity::class.java).apply {
                putExtra(MessengerActivity.EXTRA_ADDRESS, notificationSmsAddress)
                putExtra(MessengerActivity.EXTRA_CONTACT_NAME, notificationSmsAddress)
                putExtra(Constant.EXTRA_OPEN_MESSENGER_FROM_NOTIFICATION, true)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
        )
        finish()
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
