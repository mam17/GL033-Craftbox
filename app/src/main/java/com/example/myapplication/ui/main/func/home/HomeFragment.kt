package com.example.myapplication.ui.main.func.home

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.sms_helper.SmsMessageModel
import com.example.myapplication.sms_helper.SmsRepository
import com.example.myapplication.ui.components.directory.DirectoryFragment
import com.example.myapplication.ui.components.mess.activity.MessengerActivity
import com.example.myapplication.ui.main.MainActivity
import com.example.myapplication.utils.SpManager
import com.example.myapplication.utils.ViewEx.tintColor
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val smsAdapter = HomeSmsMessageAdapter()
    private val smsRepository by lazy { SmsRepository(requireContext()) }
    private var allMessages: List<SmsMessageModel> = emptyList()
    private val spManager by lazy { SpManager.get(requireContext()) }

    override fun initView() {
        applySystemBarInsets(binding.clTopBar)
        binding.rvMessages.adapter = smsAdapter
        updateNoDataState(isLoading = true)
        smsAdapter.setOnItemClick { item, _ ->
            startActivity(
                Intent(requireContext(), MessengerActivity::class.java).apply {
                    putExtra(MessengerActivity.EXTRA_THREAD_ID, item.threadId)
                    putExtra(MessengerActivity.EXTRA_ADDRESS, item.address)
                    putExtra(MessengerActivity.EXTRA_CONTACT_NAME, item.contactName)
                    putExtra(MessengerActivity.EXTRA_CONTACT_PHOTO_URI, item.contactPhotoUri)
                }
            )
        }
        binding.edtSearchHome.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        binding.edtSearchHome.setHintTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.grey
            )
        )

        binding.ivMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
        }
        binding.fabNewChat.setOnClickListener {
            (activity as? MainActivity)?.showOverlayFeatureFragment(DirectoryFragment())
        }
        binding.edtSearchHome.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) =
                Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                submitFilteredMessages(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
        binding.edtSearchHome.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideSearchKeyboardAndClearFocus(view)
                true
            } else {
                false
            }
        }
    }

    private fun applySystemBarInsets(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    override fun initData() {
        applyCurrentTheme()
    }

    override fun onResume() {
        super.onResume()
        refreshMessages()
        applyCurrentTheme()
    }

    override fun initObserver() {
        observeSmsMessages()
    }

    private fun observeSmsMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                smsRepository
                    .observeLatestMessagesByThread()
                    .collect { messages ->
                        allMessages = messages
                        submitFilteredMessages(binding.edtSearchHome.text?.toString().orEmpty())
                    }
            }
        }
    }

    fun refreshMessages() {
        viewLifecycleOwner.lifecycleScope.launch {
            val messages = smsRepository.getLatestMessagesByThread()
            allMessages = messages
            submitFilteredMessages(binding.edtSearchHome.text?.toString().orEmpty())
        }
    }

    private fun submitFilteredMessages(query: String) {
        val normalizedQuery = query.trim().lowercase()
        val filteredMessages = if (normalizedQuery.isBlank()) {
            allMessages
        } else {
            allMessages.filter { message ->
                message.address.contains(normalizedQuery, ignoreCase = true) ||
                        message.contactName.orEmpty()
                            .contains(normalizedQuery, ignoreCase = true) ||
                        message.body.contains(normalizedQuery, ignoreCase = true)
            }
        }

        smsAdapter.submitList(filteredMessages) {
            updateNoDataState(isLoading = false)
        }
    }

    private fun hideSearchKeyboardAndClearFocus(view: View) {
        view.clearFocus()
        binding.root.isFocusableInTouchMode = true
        binding.root.requestFocus()
        requireContext().getSystemService(InputMethodManager::class.java)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun updateNoDataState(isLoading: Boolean) {
        val isEmpty = smsAdapter.itemCount == 0
        binding.layoutNoData.root.isVisible = isLoading || isEmpty
        binding.layoutNoData.prLoading.isVisible = isLoading
        binding.layoutNoData.llNoData.isVisible = !isLoading && isEmpty
        binding.rvMessages.isVisible = !isLoading && !isEmpty
        binding.layoutNoData.tvBodyNoData.text = getString(R.string.txt_no_search_results_found)
    }

    private fun applyCurrentTheme() {
        val theme = spManager.getCurrentTheme() ?: return
        smsAdapter.setTheme(theme)
        binding.bgSearchHome.setImageResource(0)
        binding.bgSearchHome.setBgColor(theme.colBGEnterChat.toColorInt())
        binding.edtSearchHome.setTextColor(theme.colTextEnterChat.toColorInt())
        binding.ivMenu.tintColor(theme.colMain.toColorInt())
        binding.fabNewChat.setBgColor(theme.colMain.toColorInt())
    }
}
