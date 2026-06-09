package com.example.myapplication.ui.components.mess.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SubscriptionInfo
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.base.activity.BaseActivity
import com.example.myapplication.data.model.SmsMessage
import com.example.myapplication.databinding.ActivityMessengerBinding
import com.example.myapplication.ui.components.mess.adapter.MessengerChatAdapter
import com.example.myapplication.ui.components.mess.fragment.ChooseStickerFragment
import com.example.myapplication.ui.components.mess.fragment.MessengerDetailFragment
import com.example.myapplication.ui.components.mess.MessengerViewModel
import com.example.myapplication.ui.components.preview.activity.MediaPreviewActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.Constant
import com.example.myapplication.utils.ImageUtils
import com.example.myapplication.utils.PermissionUtils
import com.example.myapplication.data.model.ScheduledMessage
import com.example.myapplication.utils.ScheduledMessageScheduler
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max

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
    private val isOpenedFromNotification: Boolean by lazy {
        intent.getBooleanExtra(Constant.EXTRA_OPEN_MESSENGER_FROM_NOTIFICATION, false)
    }
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
    private val requestPhoneStateLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                setupSimSelector()
            } else {
                binding.llSim.isVisible = false
            }
        }

    private var isAddMenuOpen = false
    private var selectedMediaUri: Uri? = null
    private var pendingCameraUri: Uri? = null
    private var activeSimInfos: List<SubscriptionInfo> = emptyList()
    private var selectedSimIndex = 0
    private var scheduledTimestamp: Long? = null

    override fun initView() {
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        applyKeyboardInsets()
        binding.tvSenderName.text = contactName
            ?: address.ifBlank { getString(R.string.txt_unknown_sender) }
        binding.rvChat.adapter = chatAdapter
        chatAdapter.setOnMediaClick { message ->
            openMediaPreview(message)
        }
        (binding.rvChat.layoutManager as? LinearLayoutManager)?.stackFromEnd = true
        setupSimSelector()
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
                hideKeyboard()
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
            showDatePicker()
        }
        binding.llTakeCamera.setOnClickListener {
            toggleAddMenu()
            openCamera()
        }
        binding.llAttachPhoto.setOnClickListener {
            toggleAddMenu()
            openImagePicker()
        }
        binding.btnSticker.setOnClickListener {
            showChooseStickerFragment()
        }
        binding.btnRemovePreview.setOnClickListener {
            clearMediaPreview()
        }
        binding.btnRemoveSchedule.setOnClickListener {
            clearSchedulePreview()
        }
        binding.ivMore.setOnClickListener {
            if (binding.frChooseSticker.isVisible) {
                hideChooseStickerFragment()
            }
            binding.frMessDetail.visibility = View.VISIBLE
            addFragment(
                binding.frMessDetail.id,
                MessengerDetailFragment.newInstance(
                    contactName = contactName,
                    address = address,
                    contactPhotoUri = contactPhotoUri
                ),
                backStack = "detail",
                tag = "MessengerDetailFragment"
            )
        }
        binding.ivCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = "tel:${viewModel.address}".toUri()
            startActivity(intent)
        }
        binding.llSim.setOnClickListener {
            switchSelectedSim()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            if (supportFragmentManager.backStackEntryCount == 0) {
                binding.frMessDetail.visibility = View.GONE
            }
        }
    }

    private fun sendCurrentMessage() {
        val message = binding.edtMessage.text?.toString()?.trim().orEmpty()
        val mediaUri = selectedMediaUri
        val timestamp = scheduledTimestamp

        if (!hasReadySim()) {
            Toast.makeText(this, getString(R.string.txt_no_sim_available), Toast.LENGTH_SHORT).show()
            return
        }

        if (message.isBlank()) {
            Toast.makeText(this, getString(R.string.txt_please_enter_message), Toast.LENGTH_SHORT).show()
            return
        }

        if (timestamp != null && timestamp <= System.currentTimeMillis()) {
            Toast.makeText(this, getString(R.string.txt_select_future_time), Toast.LENGTH_SHORT).show()
            return
        }

        if (timestamp != null) {
            scheduleMessage(message, timestamp)
            binding.edtMessage.text?.clear()
            clearSchedulePreview()
            clearMediaPreview()
            return
        }

        if (!ensureDefaultSmsApp()) return

        binding.edtMessage.text?.clear()
        if (mediaUri != null) {
            viewModel.sendMms(this, mediaUri, message, selectedSubscriptionId())
            clearMediaPreview()
        } else {
            viewModel.sendSms(this, message, selectedSubscriptionId())
        }
    }

    private fun setupSimSelector() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_PHONE_STATE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            binding.llSim.isVisible = false
            requestPhoneStateLauncher.launch(Manifest.permission.READ_PHONE_STATE)
            return
        }

        activeSimInfos = getActiveSimInfos()
        selectedSimIndex = selectedSimIndex.coerceAtMost((activeSimInfos.size - 1).coerceAtLeast(0))
        binding.llSim.isVisible = activeSimInfos.size > 1
        updateSelectedSimUi()
    }

    @SuppressLint("MissingPermission")
    private fun getActiveSimInfos(): List<SubscriptionInfo> {
        return try {
            val subscriptionManager = getSystemService(SubscriptionManager::class.java)
            subscriptionManager?.activeSubscriptionInfoList.orEmpty()
                .sortedBy { it.simSlotIndex }
        } catch (e: SecurityException) {
            emptyList()
        }
    }

    private fun switchSelectedSim() {
        if (activeSimInfos.size <= 1) return
        selectedSimIndex = (selectedSimIndex + 1) % activeSimInfos.size
        updateSelectedSimUi()
    }

    private fun updateSelectedSimUi() {
        val simNumber = activeSimInfos
            .getOrNull(selectedSimIndex)
            ?.simSlotIndex
            ?.plus(1)
            ?: 1
        binding.tvNumberSim.text = simNumber.toString()
    }

    private fun selectedSubscriptionId(): Int {
        return activeSimInfos.getOrNull(selectedSimIndex)?.subscriptionId ?: -1
    }

    private fun applyKeyboardInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                max(systemBars.bottom, ime.bottom)
            )
            insets
        }
    }

    private fun hideKeyboard() {
        binding.edtMessage.clearFocus()
        getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(binding.edtMessage.windowToken, 0)
    }

    private fun hasReadySim(): Boolean {
        if (activeSimInfos.isNotEmpty()) return true
        return getSystemService(TelephonyManager::class.java)
            ?.simState == TelephonyManager.SIM_STATE_READY
    }

    private fun openMediaPreview(message: SmsMessage) {
        val mediaUri = message.mediaUri ?: return
        val senderName = if (message.type == android.provider.Telephony.Sms.MESSAGE_TYPE_SENT) {
            getString(R.string.txt_me)
        } else {
            contactName?.takeIf { it.isNotBlank() }
                ?: message.address.ifBlank {
                    address.ifBlank { getString(R.string.txt_unknown_sender) }
                }
        }
        startActivity(
            Intent(this, MediaPreviewActivity::class.java).apply {
                putExtra(MediaPreviewActivity.EXTRA_MEDIA_PATH, mediaUri)
                putExtra(MediaPreviewActivity.EXTRA_SENDER_NAME, senderName)
                putExtra(MediaPreviewActivity.EXTRA_SENT_TIME, message.date)
            }
        )
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
        binding.ivPreview.isVisible = true
        with(ImageUtils) {
            binding.ivPreview.loadFromPathAction(
                path = uri.toString(),
                radius = resources.getDimensionPixelSize(R.dimen.size8)
            )
        }
    }

    private fun showChooseStickerFragment() {
        if (isAddMenuOpen) toggleAddMenu()
        hideKeyboard()
        binding.frChooseSticker.visibility = View.VISIBLE

        val fragment = supportFragmentManager.findFragmentByTag(CHOOSE_STICKER_TAG)
            as? ChooseStickerFragment ?: ChooseStickerFragment().also {
            addFragment(
                binding.frChooseSticker.id,
                it,
                tag = CHOOSE_STICKER_TAG
            )
        }
        fragment.onStickerClick = { stickerPath ->
            sendSelectedSticker(stickerPath)
        }
    }

    private fun sendSelectedSticker(stickerPath: String) {
        if (!hasReadySim()) {
            Toast.makeText(this, getString(R.string.txt_no_sim_available), Toast.LENGTH_SHORT).show()
            return
        }
        if (!ensureDefaultSmsApp()) return

        val stickerUri = ImageUtils.copyAssetToInternal(this, stickerPath) ?: run {
            Toast.makeText(this, getString(R.string.txt_failed_to_process_image), Toast.LENGTH_SHORT)
                .show()
            return
        }

        viewModel.sendMms(this, stickerUri, "", selectedSubscriptionId())
        hideChooseStickerFragment()
    }

    private fun hideChooseStickerFragment() {
        binding.frChooseSticker.visibility = View.GONE
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

    private fun scheduleMessage(body: String, timestamp: Long) {
        val id = UUID.randomUUID().toString()
        val scheduledMsg = ScheduledMessage(
            id = id,
            address = address,
            contactName = contactName,
            body = body,
            scheduledTime = timestamp,
            subId = selectedSubscriptionId()
        )

        spManager.addScheduledMessage(scheduledMsg)
        ScheduledMessageScheduler.schedule(
            context = this,
            id = id,
            address = address,
            body = body,
            scheduledTime = timestamp,
            subscriptionId = selectedSubscriptionId()
        )
        Toast.makeText(this, getString(R.string.txt_scheduled), Toast.LENGTH_SHORT).show()
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

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.txt_select))
            .setTheme(R.style.CustomMaterialPicker)
            .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            showTimePicker(selection)
        }
        datePicker.show(supportFragmentManager, "DATE_PICKER")
    }

    private fun showTimePicker(dateSelection: Long) {
        val calendar = Calendar.getInstance()
        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .setTitleText(getString(R.string.txt_confirm))
            .setTheme(R.style.CustomTimePicker)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedCalendar = Calendar.getInstance().apply {
                timeInMillis = dateSelection
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
            }
            handleScheduledTime(selectedCalendar.timeInMillis)
        }
        timePicker.show(supportFragmentManager, "TIME_PICKER")
    }

    private fun handleScheduledTime(timestamp: Long) {
        if (timestamp <= System.currentTimeMillis()) {
            Toast.makeText(this, getString(R.string.txt_select_future_time), Toast.LENGTH_SHORT).show()
            return
        }
        scheduledTimestamp = timestamp
        binding.llTimeScheduled.isVisible = true

        val sdfHour = SimpleDateFormat("HH:mm", Locale.getDefault())
        val sdfDay = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        binding.tvHour.text = sdfHour.format(Date(timestamp))
        binding.tvDay.text = sdfDay.format(Date(timestamp))
    }

    private fun clearMediaPreview() {
        selectedMediaUri = null
        pendingCameraUri = null
        binding.ivPreview.setImageDrawable(null)
        binding.clMediaPreview.isVisible = false
    }

    private fun clearSchedulePreview() {
        scheduledTimestamp = null
        binding.llTimeScheduled.isVisible = false
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
        private const val CHOOSE_STICKER_TAG = "ChooseStickerFragment"
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

        if (isOpenedFromNotification) {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )
            finish()
            return
        }

        super.onBack()
    }
}
