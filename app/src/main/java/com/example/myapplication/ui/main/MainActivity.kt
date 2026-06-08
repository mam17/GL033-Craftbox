package com.example.myapplication.ui.main

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.main.func.home.HomeFragment
import com.example.myapplication.ui.main.func.SettingFragment
import com.example.myapplication.ui.main.func.StickersFragment
import com.example.myapplication.ui.main.func.ThemesFragment
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private var currentNavItemId = 0
    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            showMainFragment(currentNavItemId.takeIf { it != 0 } ?: R.id.navHome,
                forceReload = true)
        }

    override fun initView() {

        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true
        requestSmsPermissionsIfNeeded()

        binding.bnvMain.setOnItemSelectedListener { item ->
            showMainFragment(item.itemId)
        }
        binding.bnvMain.selectedItemId = R.id.navHome
        showMainFragment(R.id.navHome)

        binding.layoutDrawer.ivBack.setOnClickListener {
            closeDrawer()
        }
    }

    fun openDrawer() {
        binding.drawerLayout.open()
    }

    fun closeDrawer() {
        binding.drawerLayout.close()
    }

    override fun initData() {
    }


    override fun onBack() {
        showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_exit_app),
            strCancel = getString(R.string.txt_confirm),
            strYes = getString(R.string.txt_cancel),
            okOnClick = {},
            cancelOnClick = {
                super.onBack()
            })
    }

    override fun onResume() {
        super.onResume()
        ensureFeaturePermissions()
    }

    fun ensureFeaturePermissions(): Boolean {
        if (!PermissionUtils.hasAllPermissions(this)) {
            val fm = supportFragmentManager
            val existing = fm.findFragmentByTag(PERMISSION_FRAGMENT_TAG) as? PermissionFragment

            if (existing == null || !existing.isAdded) {
                val fragment = PermissionFragment()
                fragment.show(fm, PERMISSION_FRAGMENT_TAG)
            }
            return false
        } else {
            (supportFragmentManager.findFragmentByTag(PERMISSION_FRAGMENT_TAG) as? PermissionFragment)
                ?.dismissAllowingStateLoss()

            scheduleFullScreenNotificationDiary(this)
            return true
        }
    }

    companion object {
        private const val PERMISSION_FRAGMENT_TAG = "PermissionFragment"
    }

    private fun showMainFragment(itemId: Int): Boolean {
        return showMainFragment(itemId, forceReload = false)
    }

    private fun showMainFragment(itemId: Int, forceReload: Boolean): Boolean {
        if (!forceReload && currentNavItemId == itemId) return true

        val fragment = when (itemId) {
            R.id.navHome -> HomeFragment()
            R.id.navThemes -> ThemesFragment()
            R.id.navStickers -> StickersFragment()
            R.id.navSetting -> SettingFragment()
            else -> return false
        }

        currentNavItemId = itemId
        replaceFragment(
            containerId = R.id.frMainContent,
            fragment = fragment,
            tag = fragment.fragmentTag()
        )
        return true
    }

    private fun requestSmsPermissionsIfNeeded() {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS
        )
        val hasAllPermissions = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasAllPermissions) {
            smsPermissionLauncher.launch(permissions)
        }
    }

    private fun Fragment.fragmentTag(): String = this::class.java.simpleName
}
