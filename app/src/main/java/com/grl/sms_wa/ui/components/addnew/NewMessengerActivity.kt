package com.grl.sms_wa.ui.components.addnew

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
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.grl.sms_wa.R
import com.grl.sms_wa.base.activity.BaseActivity
import com.grl.sms_wa.data.model.ScheduledMessage
import com.grl.sms_wa.data.model.SmsMessage
import com.grl.sms_wa.databinding.ActivityNewMessengerBinding
import com.grl.sms_wa.domain.layer.DirectoryModel
import com.grl.sms_wa.domain.layer.ThemeMessModel
import com.grl.sms_wa.ui.components.mess.MessengerViewModel
import com.grl.sms_wa.ui.components.mess.adapter.MessengerChatAdapter
import com.grl.sms_wa.ui.components.mess.fragment.ChooseStickerFragment
import com.grl.sms_wa.ui.components.preview.activity.MediaPreviewActivity
import com.grl.sms_wa.utils.ContactUtils
import com.grl.sms_wa.utils.ImageUtils
import com.grl.sms_wa.utils.PermissionUtils
import com.grl.sms_wa.utils.ScheduledMessageScheduler
import com.grl.sms_wa.utils.ThemeUiHelper
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.utils.ViewEx.tintColor
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
class NewMessengerActivity :
    BaseActivity<ActivityNewMessengerBinding>(ActivityNewMessengerBinding::inflate) {
    private val viewModel: MessengerViewModel by viewModels()
    private val userAdapter = AddUserAdapter()
    private val chatAdapter: MessengerChatAdapter by lazy {
        MessengerChatAdapter(primaryContactName(), null)
    }
    private val selectedUsers = mutableListOf<DirectoryModel>()

    private val pickContactLauncher = registerForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { addContactFromUri(it) }
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

    override fun shouldApplySystemBarInsetsToRoot(): Boolean = false

    override fun initView() {
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )
        applyKeyboardInsets()
        binding.rcvUser.adapter = userAdapter
        (binding.rcvUser.layoutManager as? LinearLayoutManager)?.orientation =
            LinearLayoutManager.HORIZONTAL

        binding.rvChat.adapter = chatAdapter
        (binding.rvChat.layoutManager as? LinearLayoutManager)?.stackFromEnd = true
        chatAdapter.setOnMediaClick { message ->
            openMediaPreview(message)
        }
        userAdapter.onRemove = { user ->
            selectedUsers.removeAll { it.strPhone == user.strPhone }
            userAdapter.setData(selectedUsers)
            updateRecipients()
        }

        setupSimSelector()
        updateNoDataState(isLoading = false)

        binding.btnBack.setOnClickListener { onBack() }
        binding.btnAddUser.setOnClickListener {
            pickContactLauncher.launch(null)
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
            if (binding.frAddSticker.isVisible) {
                hideChooseStickerFragment()
            } else {
                showChooseStickerFragment()
            }
        }
        binding.btnRemovePreview.setOnClickListener {
            clearMediaPreview()
        }
        binding.btnRemoveSchedule.setOnClickListener {
            clearSchedulePreview()
        }
        binding.llSim.setOnClickListener {
            switchSelectedSim()
        }
    }

    override fun initData() {
        val address = intent.getStringExtra(EXTRA_ADDRESS).orEmpty()
        val contactName = intent.getStringExtra(EXTRA_CONTACT_NAME).orEmpty()
        applyCurrentTheme()
        if (address.isNotBlank()) {
            selectedUsers.add(
                DirectoryModel(
                    strName = contactName,
                    strPhone = address,
                    photo = ContactUtils.getContactPhoto(this, address)
                )
            )
            userAdapter.setData(selectedUsers)
            updateRecipients()
        }
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
    }

    override fun onResume() {
        super.onResume()
        applyCurrentTheme()
    }

    private fun addContactFromUri(uri: Uri) {
        val contactName = ContactUtils.getContactNameFromUri(this, uri)
        val contactNumber = ContactUtils.getContactNumberFromUri(this, uri)
        val contactPhoto = ContactUtils.getContactPhoto(this, contactNumber)

        if (contactNumber.isBlank()) return
        if (selectedUsers.any { it.strPhone == contactNumber }) return

        selectedUsers.add(
            DirectoryModel(
                strName = contactName,
                strPhone = contactNumber,
                photo = contactPhoto
            )
        )
        userAdapter.setData(selectedUsers)
        updateRecipients()
    }

    private fun updateRecipients() {
        val addresses = selectedUsers.map { it.strPhone }.filter { it.isNotBlank() }
        if (addresses.isEmpty()) {
            chatAdapter.submitList(emptyList()) {
                updateNoDataState(isLoading = false)
            }
            return
        }
        viewModel.init(
            context = this,
            address = addresses.first(),
            groupAddresses = addresses
        )
    }

    private fun sendCurrentMessage() {
        val message = binding.edtMessage.text?.toString()?.trim().orEmpty()
        val mediaUri = selectedMediaUri
        val timestamp = scheduledTimestamp
        val recipients = selectedUsers.map { it.strPhone }.filter { it.isNotBlank() }

        if (recipients.isEmpty()) {
            Toast.makeText(this, getString(R.string.txt_no_contact_found), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (!hasReadySim()) {
            Toast.makeText(this, getString(R.string.txt_no_sim_available), Toast.LENGTH_SHORT)
                .show()
            return
        }

        if (message.isBlank()) {
            Toast.makeText(
                this,
                getString(R.string.txt_please_enter_message),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        if (timestamp != null && timestamp <= System.currentTimeMillis()) {
            Toast.makeText(this, getString(R.string.txt_select_future_time), Toast.LENGTH_SHORT)
                .show()
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
        binding.frAddSticker.visibility = View.VISIBLE

        val fragment = supportFragmentManager.findFragmentByTag(CHOOSE_STICKER_TAG)
            as? ChooseStickerFragment ?: ChooseStickerFragment().also {
            addFragment(
                binding.frAddSticker.id,
                it,
                tag = CHOOSE_STICKER_TAG
            )
        }
        fragment.onStickerClick = { stickerPath ->
            sendSelectedSticker(stickerPath)
        }
    }

    private fun sendSelectedSticker(stickerPath: String) {
        val recipients = selectedUsers.map { it.strPhone }.filter { it.isNotBlank() }
        if (recipients.isEmpty()) {
            Toast.makeText(this, getString(R.string.txt_no_contact_found), Toast.LENGTH_SHORT)
                .show()
            return
        }
        if (!hasReadySim()) {
            Toast.makeText(this, getString(R.string.txt_no_sim_available), Toast.LENGTH_SHORT)
                .show()
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
        binding.frAddSticker.visibility = View.GONE
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
        selectedUsers.forEach { user ->
            val id = UUID.randomUUID().toString()
            val scheduledMsg = ScheduledMessage(
                id = id,
                address = user.strPhone,
                contactName = user.strName,
                body = body,
                scheduledTime = timestamp,
                subId = selectedSubscriptionId()
            )

            spManager.addScheduledMessage(scheduledMsg)
            ScheduledMessageScheduler.schedule(
                context = this,
                id = id,
                address = user.strPhone,
                body = body,
                scheduledTime = timestamp,
                subscriptionId = selectedSubscriptionId()
            )
        }
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
            Toast.makeText(this, getString(R.string.txt_select_future_time), Toast.LENGTH_SHORT)
                .show()
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

    private fun openMediaPreview(message: SmsMessage) {
        val mediaUri = message.mediaUri ?: return
        val senderName = if (message.type == android.provider.Telephony.Sms.MESSAGE_TYPE_SENT) {
            getString(R.string.txt_me)
        } else {
            primaryContactName().ifBlank { message.address }
        }
        startActivity(
            Intent(this, MediaPreviewActivity::class.java).apply {
                putExtra(MediaPreviewActivity.EXTRA_MEDIA_PATH, mediaUri)
                putExtra(MediaPreviewActivity.EXTRA_SENDER_NAME, senderName)
                putExtra(MediaPreviewActivity.EXTRA_SENT_TIME, message.date)
            }
        )
    }

    private fun updateNoDataState(isLoading: Boolean) {
        val isEmpty = chatAdapter.itemCount == 0
        binding.layoutNoData.root.isVisible = isLoading || isEmpty
        binding.layoutNoData.prLoading.isVisible = isLoading
        binding.layoutNoData.llNoData.isVisible = !isLoading && isEmpty
        binding.rvChat.isVisible = !isLoading && !isEmpty
        binding.layoutNoData.tvBodyNoData.text = getString(R.string.txt_no_messages_received_yet)
    }

    private fun primaryContactName(): String {
        return selectedUsers.firstOrNull()?.strName.orEmpty()
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
            binding.llToolbarMessage.setPadding(
                binding.llToolbarMessage.paddingLeft,
                systemBars.top + resources.getDimensionPixelSize(R.dimen.size8),
                binding.llToolbarMessage.paddingRight,
                binding.llToolbarMessage.paddingBottom
            )
            view.setPadding(systemBars.left, 0, systemBars.right, 0)
            binding.clBottomBar.translationY = -max(systemBars.bottom, ime.bottom).toFloat()
            binding.frAddSticker.translationY = -max(systemBars.bottom, ime.bottom).toFloat()
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

    override fun onBack() {
        if (binding.frAddSticker.isVisible) {
            hideChooseStickerFragment()
            return
        }
        if (isAddMenuOpen) {
            toggleAddMenu()
            return
        }
        finish()
    }

    companion object {
        const val EXTRA_ADDRESS = "extra_address"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        private const val CHOOSE_STICKER_TAG = "ChooseStickerFragment"
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        binding.root.applyThemeFont(theme.font)
        userAdapter.setTheme(theme)
        chatAdapter.setTheme(theme)
        ThemeUiHelper.bindBackground(binding.backgroundTheme, theme)
        binding.btnBack.tintColor(theme.colMain.toColorInt())
        binding.btnAddUser.tintColor(theme.colMain.toColorInt())
        binding.btnSticker.tintColor(theme.colMain.toColorInt())
        binding.ivSend.tintColor(theme.colMain.toColorInt())
        binding.ivSim.tintColor(theme.colMain.toColorInt())
        binding.tvNumberSim.setTextColor(theme.colMain.toColorInt())
        binding.ivCamera.tintColor(theme.colMain.toColorInt())
        binding.ivSchedule.tintColor(theme.colMain.toColorInt())
        binding.ivImage.tintColor(theme.colMain.toColorInt())
        binding.layoutNoData.tvBodyNoData.setTextColor(theme.colMain.toColorInt())
        binding.ivAdd.backgroundTintList = ThemeUiHelper.colorState(theme.colMain.toColorInt())
        binding.btnRemovePreview.backgroundTintList =
            ThemeUiHelper.colorState(theme.colMain.toColorInt())
        binding.btnRemoveSchedule.backgroundTintList =
            ThemeUiHelper.colorState(theme.colMain.toColorInt())
        binding.llInput.backgroundTintList =
            ThemeUiHelper.colorState(theme.colBGEnterChat.toColorInt())
        binding.edtMessage.setTextColor(theme.colTextEnterChat.toColorInt())
        binding.edtMessage.setHintTextColor(theme.colTextEnterChat.toColorInt())
        updateMenuTheme(theme)
    }

    private fun updateMenuTheme(theme: ThemeMessModel) {
        val mainColor = theme.colMain.toColorInt()
        binding.llAddMenu.applyThemeFont(theme.font)
        listOf(
            binding.llSchedule,
            binding.llTakeCamera,
            binding.llAttachPhoto
        ).forEach { row ->
            row.getChildAt(1)?.let { label ->
                if (label is android.widget.TextView) {
                    label.setTextColor(mainColor)
                }
            }
        }
    }
}
