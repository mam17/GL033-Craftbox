package com.grl.sms_wa.ui.main.draws.archived

import androidx.core.view.isVisible
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.data.repository.SmsRepository
import com.grl.sms_wa.databinding.FragmentArchivedBinding
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.utils.ContactUtils
import com.grl.sms_wa.utils.DialogEx.showDialogAlert
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.setupSwipeActions

class ArchivedFragment : BaseFragment<FragmentArchivedBinding>(FragmentArchivedBinding::inflate) {
    private val archivedAdapter = ArchivedAdapter()
    private val smsRepository = SmsRepository()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.rvArchive.adapter = archivedAdapter
        setupToolbar()
        setupSwipeActions()
    }

    private fun setupToolbar() {
        binding.apply {
            toolbarArchive.btnBack.setOnClickListener {
                (activity as? MainActivity)?.closeDrawerFeatureFragment(this@ArchivedFragment)
                    ?: removeFragment(this@ArchivedFragment)
            }
            toolbarArchive.tvTitle.text = getString(R.string.txt_archived)
            toolbarArchive.btnAction.gone()
            llNoData.tvBodyNoData.text = getString(R.string.txt_no_phone_numbers_are_archive)
        }
    }

    override fun initData() {
        reloadArchivedList()
    }

    private fun reloadArchivedList() {
        val archivedItems = spManager.getArchivedAddresses().map { address ->
            val contactInfo = ContactUtils.getContactInfo(requireContext(), address)
            ArchivedContactModel(
                address = address,
                displayName = contactInfo.name.ifBlank { address },
                hasContactName = contactInfo.name.isNotBlank(),
                photo = contactInfo.photo
            )
        }
        archivedAdapter.submitList(archivedItems) {
            updateNoDataState(archivedItems.isEmpty())
        }
    }

    private fun updateNoDataState(isEmpty: Boolean) {
        binding.rvArchive.isVisible = !isEmpty
        binding.llNoData.root.isVisible = isEmpty
        binding.llNoData.prLoading.isVisible = false
        binding.llNoData.llNoData.isVisible = isEmpty
    }

    private fun setupSwipeActions() {
        binding.rvArchive.setupSwipeActions(
            context = requireContext(),
            leftIconRes = R.drawable.ic_delete,
            leftLabel = getString(R.string.txt_delete_conversation),
            rightIconRes = R.drawable.ic_unarchive,
            rightLabel = getString(R.string.txt_unarchive),
            onSwipeLeft = { position ->
                archivedAdapter.currentList.getOrNull(position)?.let { item ->
                    confirmDeleteConversation(item, position)
                }
            },
            onSwipeRight = { position ->
                archivedAdapter.currentList.getOrNull(position)?.let { item ->
                    unarchiveAddress(item)
                }
            }
        )
    }

    private fun confirmDeleteConversation(item: ArchivedContactModel, position: Int) {
        archivedAdapter.notifyItemChanged(position)
        requireActivity().showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_delete_conversation),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_confirm),
            okOnClick = {
                smsRepository.deleteConversation(requireContext(), item.address.addressVariants())
                spManager.setArchived(item.address, false)
                removeArchivedItem(item)
                (activity as? MainActivity)?.refreshHomeMessages()
            }
        )
    }

    private fun unarchiveAddress(item: ArchivedContactModel) {
        spManager.setArchived(item.address, false)
        removeArchivedItem(item)
        (activity as? MainActivity)?.refreshHomeMessages()
    }

    private fun removeArchivedItem(item: ArchivedContactModel) {
        val updatedItems = archivedAdapter.currentList.filterNot { it.address == item.address }
        archivedAdapter.submitList(updatedItems) {
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
