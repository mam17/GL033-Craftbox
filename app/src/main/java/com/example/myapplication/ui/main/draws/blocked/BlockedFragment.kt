package com.example.myapplication.ui.main.draws.blocked

import androidx.core.view.isVisible
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.data.repository.SmsRepository
import com.example.myapplication.databinding.FragmentBlockedBinding
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.ContactUtils
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.setupSwipeActions
import com.example.myapplication.utils.ViewEx.gone

class BlockedFragment :
    BaseFragment<FragmentBlockedBinding>(FragmentBlockedBinding::inflate) {
    private val blockedAdapter = BlockedAdapter()
    private val smsRepository = SmsRepository()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.rvBlocked.adapter = blockedAdapter
        setupToolbar()
        setupSwipeActions()
    }

    private fun setupToolbar() {
        binding.apply {
            toolbarBlocked.btnBack.setOnClickListener {
                (activity as? MainActivity)?.closeDrawerFeatureFragment(this@BlockedFragment)
                    ?: removeFragment(this@BlockedFragment)
            }
            toolbarBlocked.tvTitle.text = getString(R.string.txt_blocked)
            toolbarBlocked.btnAction.gone()
            llNoData.tvBodyNoData.text = getString(R.string.txt_no_phone_numbers_are_blocked)
        }
    }

    override fun initData() {
        reloadBlockedList()
    }

    private fun reloadBlockedList() {
        val blockedItems = spManager.getBlockedAddresses().map { address ->
            val contactInfo = ContactUtils.getContactInfo(requireContext(), address)
            BlockedContactModel(
                address = address,
                displayName = contactInfo.name.ifBlank { address },
                hasContactName = contactInfo.name.isNotBlank(),
                photo = contactInfo.photo
            )
        }
        blockedAdapter.submitList(blockedItems) {
            updateNoDataState(blockedItems.isEmpty())
        }
    }

    private fun updateNoDataState(isEmpty: Boolean) {
        binding.rvBlocked.isVisible = !isEmpty
        binding.llNoData.root.isVisible = isEmpty
        binding.llNoData.prLoading.isVisible = false
        binding.llNoData.llNoData.isVisible = isEmpty
        
    }

    private fun setupSwipeActions() {
        binding.rvBlocked.setupSwipeActions(
            context = requireContext(),
            leftIconRes = R.drawable.ic_delete,
            leftLabel = getString(R.string.txt_delete_conversation),
            rightIconRes = R.drawable.ic_unblock,
            rightLabel = getString(R.string.txt_unblock),
            onSwipeLeft = { position ->
                blockedAdapter.currentList.getOrNull(position)?.let { item ->
                    confirmDeleteConversation(item, position)
                }
            },
            onSwipeRight = { position ->
                blockedAdapter.currentList.getOrNull(position)?.let { item ->
                    unblockAddress(item)
                }
            }
        )
    }

    private fun confirmDeleteConversation(item: BlockedContactModel, position: Int) {
        blockedAdapter.notifyItemChanged(position)
        requireActivity().showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_delete_conversation),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_confirm),
            okOnClick = {
                smsRepository.deleteConversation(requireContext(), item.address.addressVariants())
                spManager.setBlocked(item.address, false)
                removeBlockedItem(item)
                (activity as? MainActivity)?.refreshHomeMessages()
            }
        )
    }

    private fun unblockAddress(item: BlockedContactModel) {
        spManager.setBlocked(item.address, false)
        removeBlockedItem(item)
        (activity as? MainActivity)?.refreshHomeMessages()
    }

    private fun removeBlockedItem(item: BlockedContactModel) {
        val updatedItems = blockedAdapter.currentList.filterNot { it.address == item.address }
        blockedAdapter.submitList(updatedItems) {
            updateNoDataState(updatedItems.isEmpty())
        }
    }

    private fun String.addressVariants(): List<String> {
        val variants = mutableSetOf(this)
        if (startsWith("0") && length > 1) {
            variants.add("+84${substring(1)}")
        } else if (startsWith("+84") && length > 3) {
            variants.add("0${substring(3)}")
        }
        return variants.toList()
    }
}
