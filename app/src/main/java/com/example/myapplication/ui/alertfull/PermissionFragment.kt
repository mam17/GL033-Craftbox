package com.example.myapplication.ui.alertfull

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.example.myapplication.base.fragment.BaseBottomFragment
import com.example.myapplication.databinding.FragmentPermissionBinding
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.utils.PermissionUtils.openFullScreenIntentSettings

class PermissionFragment : BaseBottomFragment<FragmentPermissionBinding>() {
    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentPermissionBinding {
        return FragmentPermissionBinding.inflate(layoutInflater, container, false)
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            refreshPermissionState()
        }

    override fun initView() {
        setBottomSheetHeight(0.5f)
        binding.apply {
            updateSwitchStates()

            llNotification.setOnClickListener {
                PermissionUtils.requestNotificationPermission(
                    requireContext(),
                    notificationPermissionLauncher
                )
            }

            llFullIntent.setOnClickListener {
                if (!PermissionUtils.hasFullScreenIntentPermission(requireActivity())) {
                    openFullScreenIntentSettings(requireActivity())
                }
            }

            llExactAlarm.setOnClickListener {
                PermissionUtils.requestExactAlarmPermission(requireContext())
            }

//            llOverlay.setOnClickListener {
//                PermissionUtils.requestOverlayPermission(requireContext())
//            }

            btnGoSetting.setOnClickListener {
//                MyApplication.isAppOpenShowing = true
                PermissionUtils.openAppSettings(requireContext())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshPermissionState()
//        MyApplication.isAppOpenShowing = false
    }

    private fun refreshPermissionState() {
        updateSwitchStates()
        if (PermissionUtils.hasAllPermissions(requireContext())) {
            NotificationFSUtil.scheduleFullScreenNotificationDiary(requireContext())
            dismissAllowingStateLoss()
        }
    }

    private fun updateSwitchStates() = with(binding) {
        swNotification.isChecked = PermissionUtils.hasNotificationPermission(requireContext())
        swFullIntent.isChecked = PermissionUtils.hasFullScreenIntentPermission(requireContext())
        swExactAlarm.isChecked = PermissionUtils.isExactAlarmGranted(requireContext())

        llNotification.isVisible = !PermissionUtils.hasNotificationPermission(requireContext())
        llFullIntent.isVisible = !PermissionUtils.hasFullScreenIntentPermission(requireContext())
        llExactAlarm.isVisible = !PermissionUtils.isExactAlarmGranted(requireContext())
    }
}
