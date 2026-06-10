package com.example.myapplication.ui.main.func

import android.Manifest
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentSettingBinding
import com.example.myapplication.ui.dialog.DialogRate
import com.example.myapplication.ui.language.LanguageActivity
import com.example.myapplication.utils.AppEx.openAppInStore
import com.example.myapplication.utils.AppEx.showPolicyApp
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.SystemUtil
import com.example.myapplication.utils.ViewEx.applyThemeFont
import com.example.myapplication.utils.ViewEx.tintColor

class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {
    private val spManager by lazy { SpManager.get(requireContext()) }
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            updateNotificationState()
        }

    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
        binding.apply {
            llNotification.setOnClickListener {
                handleNotificationClick()
            }
            llLanguage.setOnClickListener {
                startNextActivity(LanguageActivity::class.java)
            }
            llPolicy.setOnClickListener { requireContext().showPolicyApp() }
            llRateUs.setOnClickListener { actionRate() }
        }
    }

    private fun applySystemBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    override fun initData() {
        applyCurrentTheme()
        bindSelectedLanguage()
        updateNotificationState()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
        bindSelectedLanguage()
        updateNotificationState()
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        val mainColor = theme.colMain.toColorInt()
        binding.root.applyThemeFont(theme.font)
        binding.tvTitle.setTextColor(mainColor)
        binding.root.applyThemeIconTint(mainColor)
    }

    private fun bindSelectedLanguage() {
        val language = SystemUtil.getLanguageModel(requireContext())
        binding.tvLangSelected.setText(language.nameRes)
    }

    private fun handleNotificationClick() {
        val context = context ?: return
        if (PermissionUtils.hasNotificationPermission(context)) {
            binding.ivNotifications.isSelected = true
            return
        }
        if (PermissionUtils.isNotificationPermissionGranted(context)) {
            PermissionUtils.openAppSettings(context)
            return
        }
        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    private fun updateNotificationState() {
        val context = context ?: return
        binding.ivNotifications.isSelected = PermissionUtils.hasNotificationPermission(context)
    }

    private fun View.applyThemeIconTint(color: Int) {
        if (this is ImageView) {
            tintColor(color)
        }
        if (this is ViewGroup) {
            for (index in 0 until childCount) {
                getChildAt(index).applyThemeIconTint(color)
            }
        }
    }

    private fun actionRate() {
        val dialogRate = DialogRate(
            requireActivity(),
            onRating = {
//                App.isAppOpenShowing = true
                showToast(getString(R.string.txt_thanks_for_your_feedback))
                requireActivity().openAppInStore()
            },
            onQuit = { showToast(getString(R.string.txt_thanks_for_your_feedback)) })
        dialogRate.show()
    }
}
