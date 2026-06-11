package com.grl.sms_wa.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.pm.PackageManager
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.facebook.shimmer.BuildConfig
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.databinding.ActivityMainBinding
import com.grl.sms_wa.ui.alertfull.NotificationFSUtil
import com.grl.sms_wa.ui.alertfull.NotificationFSUtil.scheduleFullScreenNotificationDiary
import com.grl.sms_wa.ui.alertfull.PermissionFragment
import com.grl.sms_wa.ui.components.directory.DirectoryFragment
import com.grl.sms_wa.ui.main.draws.archived.ArchivedFragment
import com.grl.sms_wa.ui.main.draws.blocked.BlockedFragment
import com.grl.sms_wa.ui.main.draws.password.SetPasswordFragment
import com.grl.sms_wa.ui.main.draws.scheduled.ScheduledFragment
import com.grl.sms_wa.ui.main.func.home.HomeFragment
import com.grl.sms_wa.ui.main.func.SettingFragment
import com.grl.sms_wa.ui.main.func.sticker.StickersFragment
import com.grl.sms_wa.ui.main.func.theme.ThemesFragment
import com.grl.sms_wa.utils.DialogEx.showDialogAlert
import com.grl.sms_wa.utils.PermissionUtils
import com.grl.sms_wa.utils.notification.NotificationUtils
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

    override fun shouldApplySystemBarInsetsToRoot(): Boolean = false

    override fun initView() {

        NotificationFSUtil.createNotificationChannel(this)
        NotificationUtils.cancelOnboardingReminder(this)
        spManager.isCompletedOnboarding = true
        applyCurrentTheme()

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
        applyCurrentTheme()
    }

    @SuppressLint("SetTextI18n")
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

        val versionName = BuildConfig.VERSION_NAME
        binding.layoutDrawer.tvVersion.text = getString(R.string.txt_version_v, versionName)
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

    fun showOverlayFeatureFragment(fragment: Fragment) {
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

    fun refreshCurrentMainFragmentForTheme() {
        val currentItemId = currentNavItemId.takeIf { it != 0 } ?: binding.bnvMain.selectedItemId
        showMainFragment(currentItemId, forceReload = true)
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
        applyCurrentTheme()
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
            DirectoryFragment::class.java.simpleName,
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

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        val selectedColor = theme.colMain.toColorInt()
        val defaultColor = ContextCompat.getColor(this, R.color.grey)
        val navColors = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                defaultColor
            )
        )

        binding.bnvMain.itemIconTintList = navColors
        binding.bnvMain.itemTextColor = navColors
        binding.vLineMain.setBackgroundColor(selectedColor)
    }

    private fun Fragment.fragmentTag(): String = this::class.java.simpleName
}
