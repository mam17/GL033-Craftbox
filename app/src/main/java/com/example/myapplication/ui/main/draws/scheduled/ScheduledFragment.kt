package com.example.myapplication.ui.main.draws.scheduled

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.data.model.ScheduledMessage
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.databinding.FragmentScheduledBinding
import com.example.myapplication.ui.dialog.DialogSchedule
import com.example.myapplication.ui.components.mess.activity.MessengerActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.ContactUtils
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.ScheduledMessageScheduler
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.gone
import com.example.myapplication.utils.setupSwipeActions

class ScheduledFragment :
    BaseFragment<FragmentScheduledBinding>(FragmentScheduledBinding::inflate) {
    private val scheduledAdapter = ScheduledAdapter()
    private val smsRepository = SmsRepository()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.rvScheduled.adapter = scheduledAdapter
        setupToolbar()
        setupSwipeActions()
        scheduledAdapter.setOnItemClick { item, _ ->
            showScheduleDialog(item)
        }
    }

    private fun setupToolbar() {
        binding.apply {
            toolbarScheduled.btnBack.setOnClickListener {
                (activity as? MainActivity)?.closeDrawerFeatureFragment(this@ScheduledFragment)
                    ?: removeFragment(this@ScheduledFragment)
            }
            toolbarScheduled.tvTitle.text = getString(R.string.txt_scheduled)
            toolbarScheduled.btnAction.gone()
            llNoData.tvBodyNoData.text = getString(R.string.txt_no_scheduled_messages)
        }
    }

    override fun initData() {
        reloadScheduledList()
    }

    override fun onResume() {
        super.onResume()
        reloadScheduledList()
    }

    private fun reloadScheduledList() {
        val scheduledItems = spManager.getScheduledMessages()
            .filter { !it.isSent }
            .sortedBy { it.scheduledTime }
            .map { message ->
                val contactInfo = ContactUtils.getContactInfo(requireContext(), message.address)
                ScheduledMessageItem(
                    id = message.id,
                    address = message.address,
                    displayName = message.contactName
                        ?.takeIf { it.isNotBlank() }
                        ?: contactInfo.name.ifBlank { message.address },
                    hasContactName = !message.contactName.isNullOrBlank() || contactInfo.name.isNotBlank(),
                    photo = contactInfo.photo,
                    body = message.body,
                    scheduledTime = message.scheduledTime
                )
            }
        scheduledAdapter.submitList(scheduledItems) {
            updateNoDataState(scheduledItems.isEmpty())
        }
    }

    private fun updateNoDataState(isEmpty: Boolean) {
        binding.rvScheduled.isVisible = !isEmpty
        binding.llNoData.root.isVisible = isEmpty
        binding.llNoData.prLoading.isVisible = false
        binding.llNoData.llNoData.isVisible = isEmpty
    }

    private fun setupSwipeActions() {
        binding.rvScheduled.setupSwipeActions(
            context = requireContext(),
            leftIconRes = R.drawable.ic_delete,
            leftLabel = getString(R.string.txt_cancel_schedule),
            rightIconRes = R.drawable.ic_chat,
            rightLabel = getString(R.string.txt_open_conversation),
            onSwipeLeft = { position ->
                scheduledAdapter.currentList.getOrNull(position)?.let { item ->
                    confirmCancelSchedule(item, position)
                }
            },
            onSwipeRight = { position ->
                scheduledAdapter.currentList.getOrNull(position)?.let { item ->
                    scheduledAdapter.notifyItemChanged(position)
                    openConversation(item)
                }
            }
        )
    }

    private fun confirmCancelSchedule(item: ScheduledMessageItem, position: Int) {
        scheduledAdapter.notifyItemChanged(position)
        requireActivity().showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_cancel_schedule),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_confirm),
            okOnClick = {
                ScheduledMessageScheduler.cancel(requireContext(), item.id)
                spManager.removeScheduledMessage(item.id)
                reloadScheduledList()
            }
        )
    }

    private fun showScheduleDialog(item: ScheduledMessageItem) {
        val scheduledMessage = findScheduledMessage(item.id) ?: return
        DialogSchedule(requireContext()).apply {
            deleteOnClick = {
                deleteScheduledMessage(scheduledMessage.id)
            }
            sendNowOnClick = {
                sendScheduledNow(scheduledMessage)
            }
            updateOnClick = {
                showUpdateScheduleBottom(scheduledMessage)
            }
        }.show()
    }

    private fun showUpdateScheduleBottom(message: ScheduledMessage) {
        val bottomFragment = UpdateScheduleBottomFragment.newInstance(
            body = message.body,
            scheduledTime = message.scheduledTime
        ).apply {
            onUpdateSchedule = { body, scheduledTime ->
                val updatedMessage = message.copy(
                    body = body,
                    scheduledTime = scheduledTime,
                    isSent = false
                )
                ScheduledMessageScheduler.cancel(requireContext(), message.id)
                spManager.updateScheduledMessage(updatedMessage)
                ScheduledMessageScheduler.schedule(
                    context = requireContext(),
                    id = updatedMessage.id,
                    address = updatedMessage.address,
                    body = updatedMessage.body,
                    scheduledTime = updatedMessage.scheduledTime,
                    subscriptionId = updatedMessage.subId
                )
                reloadScheduledList()
            }
        }
        bottomFragment.show(parentFragmentManager, "UpdateScheduleBottomFragment")
    }

    private fun sendScheduledNow(message: ScheduledMessage) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            showToast(getString(R.string.txt_failed_to_send))
            return
        }

        try {
            getSmsManager(message.subId).sendTextMessage(
                message.address,
                null,
                message.body,
                null,
                null
            )
            smsRepository.insertSentSms(
                requireContext(),
                message.address,
                message.body,
                System.currentTimeMillis()
            )
            deleteScheduledMessage(message.id)
            Toast.makeText(requireContext(), getString(R.string.txt_message_sent), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            showToast(getString(R.string.txt_failed_to_send))
        }
    }

    private fun deleteScheduledMessage(id: String) {
        ScheduledMessageScheduler.cancel(requireContext(), id)
        spManager.removeScheduledMessage(id)
        reloadScheduledList()
    }

    private fun findScheduledMessage(id: String): ScheduledMessage? {
        return spManager.getScheduledMessages().firstOrNull { it.id == id }
    }

    private fun getSmsManager(subscriptionId: Int): SmsManager {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val smsManager = requireContext().getSystemService(SmsManager::class.java)
            if (subscriptionId != -1) {
                smsManager.createForSubscriptionId(subscriptionId)
            } else {
                smsManager
            }
        } else {
            @Suppress("DEPRECATION")
            if (subscriptionId != -1) {
                SmsManager.getSmsManagerForSubscriptionId(subscriptionId)
            } else {
                SmsManager.getDefault()
            }
        }
    }

    private fun openConversation(item: ScheduledMessageItem) {
        startActivity(
            Intent(requireContext(), MessengerActivity::class.java).apply {
                putExtra(MessengerActivity.EXTRA_ADDRESS, item.address)
                putExtra(MessengerActivity.EXTRA_CONTACT_NAME, item.displayName)
            }
        )
    }
}
