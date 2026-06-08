package com.example.myapplication.ui.main

import android.Manifest
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.alertfull.NotificationFSUtil
import com.example.myapplication.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.example.myapplication.ui.alertfull.PermissionFragment
import com.example.myapplication.ui.main.draws.archived.ArchivedFragment
import com.example.myapplication.ui.main.draws.blocked.BlockedFragment
import com.example.myapplication.ui.main.draws.password.SetPasswordFragment
import com.example.myapplication.ui.main.draws.scheduled.ScheduledFragment
import com.example.myapplication.ui.main.func.home.HomeFragment
import com.example.myapplication.ui.main.func.SettingFragment
import com.example.myapplication.ui.main.func.StickersFragment
import com.example.myapplication.ui.main.func.theme.ThemesFragment
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.notification.NotificationUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>(ActivityMainBinding::inflate) {
    private var currentNavItemId = 0
    private var isBottomNavigationVisible = true
    private var shouldRefreshHomeMessages = false
    private var hasRequestedDefaultSmsRole = false
    private val smsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            showMainFragment(currentNavItemId.takeIf { it != 0 } ?: R.id.navHome,
                forceReload = true)
            requestDefaultSmsRoleIfNeeded()
        }
    private val requestDefaultSmsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!PermissionUtils.isDefaultSmsApp(this)) {
                showToast(getString(R.string.txt_set_as_default_sms_required))
            }
        }

    override fun initView() {

        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true

        binding.bnvMain.setOnItemSelectedListener { item ->
            showMainFragment(item.itemId)
        }
        binding.bnvMain.selectedItemId = R.id.navHome
        showMainFragment(R.id.navHome)

        initDrawer()
        binding.root.post {
            if (!requestSmsPermissionsIfNeeded()) {
                requestDefaultSmsRoleIfNeeded()
            }
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

    private fun initDrawer() {
        binding.layoutDrawer.ivBack.setOnClickListener {
            closeDrawer()
        }
        binding.layoutDrawer.llArchived.setOnClickListener {
            showDrawerFragment(ArchivedFragment())
        }
        binding.layoutDrawer.llBackup.setOnClickListener {
            closeDrawer()
            showToast(getString(R.string.txt_feature_coming_soon))
        }
        binding.layoutDrawer.llPassword.setOnClickListener {
            showDrawerFragment(SetPasswordFragment())
        }
        binding.layoutDrawer.llScheduled.setOnClickListener {
            showDrawerFragment(ScheduledFragment())
        }
        binding.layoutDrawer.llBlocking.setOnClickListener {
            showDrawerFragment(BlockedFragment())
        }
    }

    private fun showDrawerFragment(fragment: Fragment) {
        removeCurrentDrawerFragment()
        currentNavItemId = 0
        closeDrawer()
        setBottomNavigationVisible(false)
        addFragment(
            containerId = R.id.frMainContent,
            fragment = fragment,
            tag = fragment.fragmentTag()
        )
    }

    fun closeDrawerFeatureFragment(fragment: Fragment? = null) {
        val targetFragment = fragment ?: findCurrentDrawerFragment() ?: return
        removeFragment(targetFragment)
        currentNavItemId = binding.bnvMain.selectedItemId.takeIf { it != 0 } ?: R.id.navHome
        setBottomNavigationVisible(true)
        refreshHomeMessagesIfNeeded()
    }

    fun refreshHomeMessages() {
        shouldRefreshHomeMessages = true
        refreshHomeMessagesIfNeeded()
    }

    override fun onBack() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
            return
        }

        if (currentNavItemId == 0) {
            closeDrawerFeatureFragment()
            return
        }

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
        if (PermissionUtils.isDefaultSmsApp(this)) {
            hasRequestedDefaultSmsRole = false
        }
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
        private const val BOTTOM_NAV_ANIMATION_DURATION = 180L
        private val DRAWER_FRAGMENT_TAGS = setOf(
            ArchivedFragment::class.java.simpleName,
            BlockedFragment::class.java.simpleName,
            SetPasswordFragment::class.java.simpleName,
            ScheduledFragment::class.java.simpleName
        )
    }

    private fun showMainFragment(itemId: Int): Boolean {
        return showMainFragment(itemId, forceReload = false)
    }

    private fun showMainFragment(itemId: Int, forceReload: Boolean): Boolean {
        if (!forceReload && currentNavItemId == itemId) return true
        removeCurrentDrawerFragment()

        val fragment = when (itemId) {
            R.id.navHome -> HomeFragment()
            R.id.navThemes -> ThemesFragment()
            R.id.navStickers -> StickersFragment()
            R.id.navSetting -> SettingFragment()
            else -> return false
        }

        currentNavItemId = itemId
        setBottomNavigationVisible(true)
        replaceFragment(
            containerId = R.id.frMainContent,
            fragment = fragment,
            tag = fragment.fragmentTag()
        )
        return true
    }

    private fun refreshHomeMessagesIfNeeded() {
        if (!shouldRefreshHomeMessages) return

        val homeFragment = supportFragmentManager.fragments
            .filterIsInstance<HomeFragment>()
            .firstOrNull()

        if (homeFragment != null && homeFragment.isAdded) {
            homeFragment.refreshMessages()
            shouldRefreshHomeMessages = false
        }
    }

    private fun setBottomNavigationVisible(isVisible: Boolean) {
        if (isBottomNavigationVisible == isVisible) return
        isBottomNavigationVisible = isVisible

        val navHeight = binding.bnvMain.height
            .takeIf { it > 0 }
            ?.toFloat()
            ?: resources.getDimension(R.dimen.size70)

        binding.bnvMain.animate().cancel()
        binding.vLineMain.animate().cancel()

        if (isVisible) {
            binding.bnvMain.visibility = View.VISIBLE
            binding.vLineMain.visibility = View.VISIBLE
            binding.bnvMain.translationY = navHeight
            binding.bnvMain.alpha = 0f
            binding.vLineMain.alpha = 0f

            binding.bnvMain.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(BOTTOM_NAV_ANIMATION_DURATION)
                .start()
            binding.vLineMain.animate()
                .alpha(1f)
                .setDuration(BOTTOM_NAV_ANIMATION_DURATION)
                .start()
        } else {
            binding.bnvMain.animate()
                .translationY(navHeight)
                .alpha(0f)
                .setDuration(BOTTOM_NAV_ANIMATION_DURATION)
                .withEndAction {
                    binding.bnvMain.visibility = View.GONE
                    binding.bnvMain.translationY = 0f
                }
                .start()
            binding.vLineMain.animate()
                .alpha(0f)
                .setDuration(BOTTOM_NAV_ANIMATION_DURATION)
                .withEndAction {
                    binding.vLineMain.visibility = View.GONE
                }
                .start()
        }
    }

    private fun removeCurrentDrawerFragment() {
        findCurrentDrawerFragment()?.let { removeFragment(it) }
    }

    private fun findCurrentDrawerFragment(): Fragment? {
        return DRAWER_FRAGMENT_TAGS
            .asSequence()
            .mapNotNull { tag -> supportFragmentManager.findFragmentByTag(tag) }
            .firstOrNull { it.isAdded }
    }

    private fun requestSmsPermissionsIfNeeded(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )
        val hasAllPermissions = permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }

        if (!hasAllPermissions) {
            smsPermissionLauncher.launch(permissions)
            return true
        }
        return false
    }

    private fun requestDefaultSmsRoleIfNeeded() {
        if (PermissionUtils.isDefaultSmsApp(this)) {
            hasRequestedDefaultSmsRole = false
            return
        }
        if (hasRequestedDefaultSmsRole) return

        val requestIntent = PermissionUtils.getRequestDefaultSmsIntent(this)
        if (requestIntent != null) {
            hasRequestedDefaultSmsRole = true
            requestDefaultSmsLauncher.launch(requestIntent)
        } else {
            showToast(getString(R.string.txt_set_as_default_sms_required))
        }
    }

    private fun Fragment.fragmentTag(): String = this::class.java.simpleName
}
