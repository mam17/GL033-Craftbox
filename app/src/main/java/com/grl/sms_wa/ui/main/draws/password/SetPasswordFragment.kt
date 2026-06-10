package com.grl.sms_wa.ui.main.draws.password

import androidx.core.view.isVisible
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.data.repository.SmsRepository
import com.grl.sms_wa.databinding.FragmentSetPasswordBinding
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.utils.ContactUtils
import com.grl.sms_wa.utils.DialogEx.showDialogAlert
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.gone
import com.grl.sms_wa.utils.setupSwipeActions

class SetPasswordFragment :
    BaseFragment<FragmentSetPasswordBinding>(FragmentSetPasswordBinding::inflate) {
    private val passwordAdapter = PasswordAdapter()
    private val smsRepository = SmsRepository()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        binding.rvPassword.adapter = passwordAdapter
        setupToolbar()
        setupSwipeActions()
    }

    private fun setupToolbar() {
        binding.apply {
            toolbarPassword.btnBack.setOnClickListener {
                (activity as? MainActivity)?.closeDrawerFeatureFragment(this@SetPasswordFragment)
                    ?: removeFragment(this@SetPasswordFragment)
            }
            toolbarPassword.tvTitle.text = getString(R.string.txt_set_password)
            toolbarPassword.btnAction.gone()
            llNoData.tvBodyNoData.text = getString(R.string.txt_no_numbers_are_password_protected)
        }
    }

    override fun initData() {
        reloadPasswordList()
    }

    private fun reloadPasswordList() {
        val passwordItems = spManager.getPasswordAddresses().map { address ->
            val contactInfo = ContactUtils.getContactInfo(requireContext(), address)
            PasswordContactModel(
                address = address,
                displayName = contactInfo.name.ifBlank { address },
                hasContactName = contactInfo.name.isNotBlank(),
                photo = contactInfo.photo
            )
        }
        passwordAdapter.submitList(passwordItems) {
            updateNoDataState(passwordItems.isEmpty())
        }
    }

    private fun updateNoDataState(isEmpty: Boolean) {
        binding.rvPassword.isVisible = !isEmpty
        binding.llNoData.root.isVisible = isEmpty
        binding.llNoData.prLoading.isVisible = false
        binding.llNoData.llNoData.isVisible = isEmpty
    }

    private fun setupSwipeActions() {
        binding.rvPassword.setupSwipeActions(
            context = requireContext(),
            leftIconRes = R.drawable.ic_delete,
            leftLabel = getString(R.string.txt_delete_conversation),
            rightIconRes = R.drawable.ic_remove_password,
            rightLabel = getString(R.string.txt_remove_password),
            onSwipeLeft = { position ->
                passwordAdapter.currentList.getOrNull(position)?.let { item ->
                    confirmDeleteConversation(item, position)
                }
            },
            onSwipeRight = { position ->
                passwordAdapter.currentList.getOrNull(position)?.let { item ->
                    removePassword(item)
                }
            }
        )
    }

    private fun confirmDeleteConversation(item: PasswordContactModel, position: Int) {
        passwordAdapter.notifyItemChanged(position)
        requireActivity().showDialogAlert(
            strTitle = getString(R.string.txt_are_you_sure),
            strBody = getString(R.string.txt_delete_conversation),
            strCancel = getString(R.string.txt_cancel),
            strYes = getString(R.string.txt_confirm),
            okOnClick = {
                smsRepository.deleteConversation(requireContext(), item.address.addressVariants())
                reloadPasswordList()
            }
        )
    }

    private fun removePassword(item: PasswordContactModel) {
        spManager.setConversationPassword(item.address, null)
        reloadPasswordList()
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
