package com.example.myapplication.ui.components.mess.fragment

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentMessengerDetailBinding
import com.example.myapplication.ui.components.mess.MessengerViewModel
import com.example.myapplication.utils.DialogEx.showDialogAlert

class MessengerDetailFragment :
    BaseFragment<FragmentMessengerDetailBinding>(FragmentMessengerDetailBinding::inflate) {

    private val viewModel: MessengerViewModel by activityViewModels()
    private var contactName: String? = null

    override fun initView() {
        contactName = arguments?.getString(EXTRA_CONTACT_NAME)
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.tvName.text = contactName ?: getString(R.string.txt_unknown_sender)

        binding.llBackground.setOnClickListener {
            // Handle background change
        }

        binding.llSetPassword.setOnClickListener {
            addFragment(
                R.id.frMessDetail,
                MessengerPasswordFragment.newInstance(),
                backStack = "password_set"
            )
        }

        binding.llNotifications.setOnClickListener {
            viewModel.toggleNotifications()
        }

        binding.llArchive.setOnClickListener {
            requireActivity().showDialogAlert(
                strTitle = getString(R.string.txt_are_you_sure),
                strBody = getString(R.string.txt_archive),
                strCancel = getString(R.string.txt_cancel),
                strYes = getString(R.string.txt_confirm),
                okOnClick = {
                    viewModel.archiveConversation()
                    parentFragmentManager.popBackStack()
                }
            )
        }

        binding.llUnarchive.setOnClickListener {
            viewModel.toggleArchive()
        }

        binding.llBlock.setOnClickListener {
            requireActivity().showDialogAlert(
                strTitle = getString(R.string.txt_are_you_sure),
                strBody = getString(R.string.txt_block),
                strCancel = getString(R.string.txt_cancel),
                strYes = getString(R.string.txt_confirm),
                okOnClick = {
                    viewModel.blockConversation()
                    parentFragmentManager.popBackStack()
                }
            )
        }

        binding.llUnblock.setOnClickListener {
            viewModel.toggleBlock()
        }

        binding.llDelete.setOnClickListener {
            requireActivity().showDialogAlert(
                strTitle = getString(R.string.txt_are_you_sure),
                strBody = getString(R.string.txt_delete_conversation),
                strCancel = getString(R.string.txt_cancel),
                strYes = getString(R.string.txt_confirm),
                okOnClick = {
                    viewModel.deleteConversation(requireContext())
                    parentFragmentManager.popBackStack()
                }
            )
        }

        viewModel.isArchived.observe(viewLifecycleOwner) { isArchived ->
            binding.llArchive.isVisible = isArchived != true
            binding.llUnarchive.isVisible = isArchived == true
        }
        viewModel.isBlocked.observe(viewLifecycleOwner) { isBlocked ->
            binding.llBlock.isVisible = isBlocked != true
            binding.llUnblock.isVisible = isBlocked == true
        }
        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            binding.llNotifications.alpha = if (enabled == false) 0.5f else 1f
        }
    }

    override fun initData() {
        arguments?.let {
            contactName = it.getString(EXTRA_CONTACT_NAME)
        }
    }

    companion object {
        const val EXTRA_CONTACT_NAME = "extra_contact_name"

        fun newInstance(contactName: String?): MessengerDetailFragment {
            val fragment = MessengerDetailFragment()
            val args = Bundle()
            args.putString(EXTRA_CONTACT_NAME, contactName)
            fragment.arguments = args
            return fragment
        }
    }
}
