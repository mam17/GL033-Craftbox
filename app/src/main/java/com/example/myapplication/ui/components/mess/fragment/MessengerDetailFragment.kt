package com.example.myapplication.ui.components.mess.fragment

import android.os.Bundle
import android.content.Intent
import android.provider.Telephony
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentMessengerDetailBinding
import com.example.myapplication.ui.components.mess.MessageItem
import com.example.myapplication.ui.components.mess.MessengerViewModel
import com.example.myapplication.ui.components.mess.adapter.MessengerMediaAdapter
import com.example.myapplication.ui.components.mess.adapter.MessengerMediaItem
import com.example.myapplication.ui.components.preview.activity.MediaPreviewActivity
import com.example.myapplication.utils.DialogEx.showDialogAlert
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.tintColor

class MessengerDetailFragment :
    BaseFragment<FragmentMessengerDetailBinding>(FragmentMessengerDetailBinding::inflate) {

    private val viewModel: MessengerViewModel by activityViewModels()
    private val mediaAdapter = MessengerMediaAdapter()
    private var contactName: String? = null
    private var address: String? = null
    private var contactPhotoUri: String? = null

    override fun initView() {
        readArguments()
        bindContactHeader()
        setupMediaList()
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.llBackground.setOnClickListener {
            // Handle background change
        }

        binding.llSetPassword.setOnClickListener {
            val mode = if (viewModel.hasPassword.value == true) {
                MessengerPasswordFragment.MODE_REMOVE
            } else {
                MessengerPasswordFragment.MODE_SET
            }
            addFragment(
                R.id.frMessDetail,
                MessengerPasswordFragment.newInstance(mode = mode),
                backStack = "password_set"
            )
        }

        binding.llNotifications.setOnClickListener {
            viewModel.toggleNotifications()
        }

        binding.llArchive.setOnClickListener {
            requireActivity().showDialogAlert(
                strTitle = getString(R.string.txt_are_you_sure),
                strBody = binding.tvArchive.text.toString(),
                strCancel = getString(R.string.txt_cancel),
                strYes = getString(R.string.txt_confirm),
                okOnClick = {
                    val shouldCloseConversation = viewModel.isArchived.value != true
                    viewModel.toggleArchive()
                    if (shouldCloseConversation) {
                        requireActivity().finish()
                    }
                }
            )
        }

        binding.llBlock.setOnClickListener {
            requireActivity().showDialogAlert(
                strTitle = getString(R.string.txt_are_you_sure),
                strBody = binding.tvBlock.text.toString(),
                strCancel = getString(R.string.txt_cancel),
                strYes = getString(R.string.txt_confirm),
                okOnClick = {
                    val shouldCloseConversation = viewModel.isBlocked.value != true
                    viewModel.toggleBlock()
                    if (shouldCloseConversation) {
                        requireActivity().finish()
                    }
                }
            )
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
            bindToggleRow(
                row = binding.llArchive,
                icon = binding.ivArchive,
                textView = binding.tvArchive,
                isSelected = isArchived == true,
                selectedText = getString(R.string.txt_unarchive),
                unselectedText = getString(R.string.txt_archive)
            )
        }
        viewModel.isBlocked.observe(viewLifecycleOwner) { isBlocked ->
            bindToggleRow(
                row = binding.llBlock,
                icon = binding.ivBlock,
                textView = binding.tvBlock,
                isSelected = isBlocked == true,
                selectedText = getString(R.string.txt_unblock),
                unselectedText = getString(R.string.txt_block)
            )
        }
        viewModel.notificationsEnabled.observe(viewLifecycleOwner) { enabled ->
            bindToggleRow(
                row = binding.llNotifications,
                icon = binding.ivNotifications,
                textView = binding.tvNotifications,
                isSelected = enabled != false,
                selectedText = getString(R.string.txt_turn_notifications_off),
                unselectedText = getString(R.string.txt_turn_notifications_on)
            )
        }
        viewModel.hasPassword.observe(viewLifecycleOwner) { hasPassword ->
            bindToggleRow(
                row = binding.llSetPassword,
                icon = binding.ivPassword,
                textView = binding.tvPassword,
                isSelected = hasPassword == true,
                selectedText = getString(R.string.txt_remove_password),
                unselectedText = getString(R.string.txt_set_password)
            )
        }
        viewModel.messages.observe(viewLifecycleOwner) { items ->
            val mediaItems = items
                .mapNotNull { item ->
                    val message = (item as? MessageItem.MessageContent)?.message
                        ?: return@mapNotNull null
                    val mediaUri = message.mediaUri?.takeIf { it.isNotBlank() }
                        ?: return@mapNotNull null
                    val senderName = if (message.type == Telephony.Sms.MESSAGE_TYPE_SENT) {
                        getString(R.string.txt_me)
                    } else {
                        contactName?.takeIf { it.isNotBlank() }
                            ?: message.address.ifBlank {
                                address?.takeIf { it.isNotBlank() }
                                    ?: getString(R.string.txt_unknown_sender)
                            }
                    }
                    MessengerMediaItem(
                        path = mediaUri,
                        senderName = senderName,
                        date = message.date
                    )
                }
                .distinctBy { it.path }
            binding.rcvMedia.isVisible = mediaItems.isNotEmpty()
            mediaAdapter.submitList(mediaItems)
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    private fun readArguments() {
        arguments?.let {
            contactName = it.getString(EXTRA_CONTACT_NAME)
            address = it.getString(EXTRA_ADDRESS)
            contactPhotoUri = it.getString(EXTRA_CONTACT_PHOTO_URI)
        }
    }

    private fun bindContactHeader() {
        val displayName = contactName
            ?.takeIf { it.isNotBlank() }
            ?: address?.takeIf { it.isNotBlank() }
            ?: getString(R.string.txt_unknown_sender)
        binding.tvName.text = displayName

        if (!contactPhotoUri.isNullOrBlank()) {
            binding.ivAvatar.scaleType = ImageView.ScaleType.CENTER_CROP
            Glide.with(binding.ivAvatar)
                .load(contactPhotoUri)
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(binding.ivAvatar)
        }
    }

    private fun setupMediaList() {
        binding.rcvMedia.adapter = mediaAdapter
        binding.rcvMedia.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        mediaAdapter.setOnItemClick { item, _ ->
            startActivity(
                Intent(requireContext(), MediaPreviewActivity::class.java).apply {
                    putExtra(MediaPreviewActivity.EXTRA_MEDIA_PATH, item.path)
                    putExtra(MediaPreviewActivity.EXTRA_SENDER_NAME, item.senderName)
                    putExtra(MediaPreviewActivity.EXTRA_SENT_TIME, item.date)
                }
            )
        }
    }

    private fun applyCurrentTheme() {
        val theme = SpManager.get(requireContext()).getCurrentTheme() ?: return
        val mainColor = theme.colMain.toColorInt()

        binding.ivBack.tintColor(mainColor)
        binding.ivBackground.tintColor(mainColor)
        binding.ivPassword.tintColor(mainColor)
        binding.ivNotifications.tintColor(mainColor)
        binding.ivArchive.tintColor(mainColor)
        binding.ivBlock.tintColor(mainColor)
        binding.ivAvatar.setBgColor(mainColor)
    }

    private fun bindToggleRow(
        row: View,
        icon: ImageView,
        textView: TextView,
        isSelected: Boolean,
        selectedText: String,
        unselectedText: String
    ) {
        row.isSelected = isSelected
        icon.isSelected = isSelected
        textView.isSelected = isSelected
        textView.text = if (isSelected) selectedText else unselectedText
    }

    companion object {
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_CONTACT_PHOTO_URI = "extra_contact_photo_uri"

        fun newInstance(
            contactName: String?,
            address: String?,
            contactPhotoUri: String?
        ): MessengerDetailFragment {
            val fragment = MessengerDetailFragment()
            val args = Bundle()
            args.putString(EXTRA_CONTACT_NAME, contactName)
            args.putString(EXTRA_ADDRESS, address)
            args.putString(EXTRA_CONTACT_PHOTO_URI, contactPhotoUri)
            fragment.arguments = args
            return fragment
        }
    }
}
