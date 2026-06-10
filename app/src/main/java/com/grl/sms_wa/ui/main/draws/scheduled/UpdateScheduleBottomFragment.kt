package com.grl.sms_wa.ui.main.draws.scheduled

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseBottomFragment
import com.grl.sms_wa.databinding.FragmentUpdateScheduledBinding
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpdateScheduleBottomFragment : BaseBottomFragment<FragmentUpdateScheduledBinding>() {
    var onUpdateSchedule: ((body: String, scheduledTime: Long) -> Unit)? = null
    private var selectedTime: Long = 0L

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentUpdateScheduledBinding {
        return FragmentUpdateScheduledBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        selectedTime = requireArguments().getLong(ARG_SCHEDULED_TIME)
        binding.edtMessage.setText(requireArguments().getString(ARG_BODY).orEmpty())
        updateTimeText()

        binding.btnEditTime.setOnClickListener {
            showDatePicker()
        }
        binding.btnUpdate.setOnClickListener {
            val body = binding.edtMessage.text?.toString()?.trim().orEmpty()
            if (body.isBlank()) {
                showToast(getString(R.string.txt_please_enter_message))
                return@setOnClickListener
            }
            if (selectedTime <= System.currentTimeMillis()) {
                showToast(getString(R.string.txt_select_future_time))
                return@setOnClickListener
            }
            onUpdateSchedule?.invoke(body, selectedTime)
            dismissAllowingStateLoss()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.txt_select))
            .setTheme(R.style.CustomMaterialPicker)
            .setSelection(selectedTime)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            showTimePicker(selection)
        }
        datePicker.show(parentFragmentManager, DATE_PICKER_TAG)
    }

    private fun showTimePicker(dateSelection: Long) {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = selectedTime
        }
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(getString(R.string.txt_confirm))
            .setTheme(R.style.CustomTimePicker)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            selectedTime = Calendar.getInstance().apply {
                timeInMillis = dateSelection
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            updateTimeText()
        }
        timePicker.show(parentFragmentManager, TIME_PICKER_TAG)
    }

    private fun updateTimeText() {
        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy", Locale.getDefault())
        binding.tvTimeDate.text = dateFormat.format(Date(selectedTime))
    }

    companion object {
        private const val ARG_BODY = "arg_body"
        private const val ARG_SCHEDULED_TIME = "arg_scheduled_time"
        private const val DATE_PICKER_TAG = "UpdateScheduleDatePicker"
        private const val TIME_PICKER_TAG = "UpdateScheduleTimePicker"

        fun newInstance(body: String, scheduledTime: Long): UpdateScheduleBottomFragment {
            return UpdateScheduleBottomFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_BODY, body)
                    putLong(ARG_SCHEDULED_TIME, scheduledTime)
                }
            }
        }
    }
}
