package com.example.myapplication.ui.components.mess.activity

import android.content.Intent
import android.net.Uri
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.databinding.ActivityMessengerBinding
import com.example.myapplication.ui.components.mess.adapter.MessengerChatAdapter
import com.example.myapplication.ui.components.mess.fragment.MessengerDetailFragment
import com.example.myapplication.ui.components.mess.MessengerViewModel
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ImageUtils
import com.example.myapplication.utils.PermissionUtils
import dagger.hilt.android.AndroidEntryPoint
import java.io.File

@AndroidEntryPoint
class MessengerActivity :
    BaseActivity<ActivityMessengerBinding>(ActivityMessengerBinding::inflate) {
    private val viewModel: MessengerViewModel by viewModels()
    private val threadId: Long by lazy { intent.getLongExtra(EXTRA_THREAD_ID, INVALID_THREAD_ID) }
    private val address: String by lazy {
        intent.getStringExtra(EXTRA_ADDRESS)
            ?: intent.getStringExtra(Constant.EXTRA_ADDRESS)
            ?: ""
    }
    private val groupAddresses: List<String> by lazy {
        intent.getStringArrayListExtra(EXTRA_GROUP_ADDRESSES)
            ?: intent.getStringArrayListExtra(Constant.EXTRA_GROUP_ADDRESSES)
            ?: emptyList()
    }
    private val contactName: String? by lazy { intent.getStringExtra(EXTRA_CONTACT_NAME) }
    private val contactPhotoUri: String? by lazy { intent.getStringExtra(EXTRA_CONTACT_PHOTO_URI) }
    private val chatAdapter: MessengerChatAdapter by lazy {
        MessengerChatAdapter(contactName, contactPhotoUri)
    }
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { showMediaPreview(it) }
        }
    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            val uri = pendingCameraUri
            if (success && uri != null) {
                showMediaPreview(uri)
            } else {
                pendingCameraUri = null
            }
        }
    private val requestDefaultSmsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (!PermissionUtils.isDefaultSmsApp(this)) {
                Toast.makeText(
                    this,
                    getString(R.string.txt_set_as_default_sms_required),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    private var isAddMenuOpen = false
    private var selectedMediaUri: Uri? = null
    private var pendingCameraUri: Uri? = null

    override fun initView() {
        binding.tvSenderName.text = contactName
            ?: address.ifBlank { getString(R.string.txt_unknown_sender) }
        binding.tvNumberSim.text = "1"
        binding.rvChat.adapter = chatAdapter
        (binding.rvChat.layoutManager as? LinearLayoutManager)?.stackFromEnd = true
        updateNoDataState(isLoading = true)

        binding.ivBack.setOnClickListener {
            onBack()
        }
        binding.ivAdd.setOnClickListener {
            toggleAddMenu()
        }
        binding.ivSend.setOnClickListener {
            sendCurrentMessage()
        }
        binding.edtMessage.setOnEditorActionListener { _, actionId, event ->
            val isEnterUp = event?.keyCode == KeyEvent.KEYCODE_ENTER &&
                event.action == KeyEvent.ACTION_UP
            val shouldSend = actionId == EditorInfo.IME_ACTION_SEND || isEnterUp
            if (shouldSend) {
                sendCurrentMessage()
                true
            } else {
                false
            }
        }
        binding.vScrim.setOnClickListener {
            if (isAddMenuOpen) toggleAddMenu()
        }
        binding.llSchedule.setOnClickListener {
            toggleAddMenu()
        }
        binding.llTakeCamera.setOnClickListener {
            toggleAddMenu()
            openCamera()
        }
        binding.llAttachPhoto.setOnClickListener {
            toggleAddMenu()
            openImagePicker()
        }
        binding.btnRemovePreview.setOnClickListener {
            clearMediaPreview()
        }
        binding.ivMore.setOnClickListener {
            binding.frMessDetail.visibility = View.VISIBLE
            addFragment(
                binding.frMessDetail.id,
                MessengerDetailFragment.newInstance(contactName),
                backStack = "detail",
                tag = "MessengerDetailFragment"
            )
        }
        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = "tel:${viewModel.address}".toUri()
            startActivity(intent)
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.frMessDetail.visibility = View.GONE
            }
        }
    }

    private fun sendCurrentMessage() {
        if (!ensureDefaultSmsApp()) return

        val message = binding.edtMessage.text?.toString()?.trim().orEmpty()
        val mediaUri = selectedMediaUri
        if (message.isBlank() && mediaUri == null) return

        binding.edtMessage.text?.clear()
        if (mediaUri != null) {
            viewModel.sendMms(this, mediaUri, message)
            clearMediaPreview()
        } else {
            viewModel.sendSms(this, message)
        }
    }

    private fun ensureDefaultSmsApp(): Boolean {
        if (PermissionUtils.isDefaultSmsApp(this)) return true

        val requestIntent = PermissionUtils.getRequestDefaultSmsIntent(this)
        if (requestIntent != null) {
            requestDefaultSmsLauncher.launch(requestIntent)
        } else {
            Toast.makeText(
                this,
                getString(R.string.txt_set_as_default_sms_required),
                Toast.LENGTH_SHORT
            ).show()
        }
        return false
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun openCamera() {
        val uri = createCameraImageUri()
        pendingCameraUri = uri
        takePictureLauncher.launch(uri)
    }

    private fun showMediaPreview(uri: Uri) {
        selectedMediaUri = uri
        binding.clMediaPreview.isVisible = true
        with(ImageUtils) {
            binding.ivPreview.loadFromPathAction(
                path = uri.toString(),
                radius = resources.getDimensionPixelSize(R.dimen.size8)
            )
        }
    }

    private fun clearMediaPreview() {
        selectedMediaUri = null
        pendingCameraUri = null
        binding.ivPreview.setImageDrawable(null)
        binding.clMediaPreview.isVisible = false
    }

    private fun createCameraImageUri(): Uri {
        val imageDir = File(cacheDir, "shared_images").apply { mkdirs() }
        val imageFile = File(imageDir, "camera_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            this,
            "$packageName.fileprovider",
            imageFile
        )
    }

    private fun toggleAddMenu() {
        isAddMenuOpen = !isAddMenuOpen
        if (isAddMenuOpen) {
            binding.ivAdd.animate().rotation(45f).setDuration(200).start()
            binding.vScrim.visibility = View.VISIBLE
            binding.llAddMenu.visibility = View.VISIBLE
        } else {
            binding.ivAdd.animate().rotation(0f).setDuration(200).start()
            binding.vScrim.visibility = View.GONE
            binding.llAddMenu.visibility = View.GONE
        }
    }

    override fun initData() {
        if (address.isBlank()) {
            finish()
            return
        }
        viewModel.init(this, address, groupAddresses, threadId)
    }

    override fun initObserver() {
        viewModel.messages.observe(this) { messages ->
            chatAdapter.submitList(messages) {
                updateNoDataState(isLoading = false)
                if (messages.isNotEmpty()) {
                    binding.rvChat.scrollToPosition(messages.lastIndex)
                }
            }
        }
        viewModel.conversationDeleted.observe(this) { isDeleted ->
            if (isDeleted == true) {
                finish()
            }
        }
    }

    private fun updateNoDataState(isLoading: Boolean) {
        val isEmpty = chatAdapter.itemCount == 0
        binding.layoutNoData.root.isVisible = isLoading || isEmpty
        binding.layoutNoData.prLoading.isVisible = isLoading
        binding.layoutNoData.llNoData.isVisible = !isLoading && isEmpty
        binding.rvChat.isVisible = !isLoading && !isEmpty
    }

    companion object {
        const val EXTRA_THREAD_ID = "extra_thread_id"
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_GROUP_ADDRESSES = "extra_group_addresses"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_CONTACT_PHOTO_URI = "extra_contact_photo_uri"
        private const val INVALID_THREAD_ID = -1L
    }

    override fun onBack() {
        if (binding.frChooseSticker.isVisible) {
            binding.frChooseSticker.visibility = View.GONE
            return
        }

        if (binding.frMessDetail.isVisible || supportFragmentManager.backStackEntryCount > 0) {
            if (supportFragmentManager.backStackEntryCount > 0) {
                supportFragmentManager.popBackStack()
            } else {
                binding.frMessDetail.visibility = View.GONE
            }
            return
        }

        super.onBack()
    }
}
