package com.example.myapplication.ui.main.func.home

import android.view.View
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.sms_helper.SmsRepository
import com.example.myapplication.ui.components.mess.activity.MessengerActivity
import com.example.myapplication.ui.main.MainActivity
import kotlinx.coroutines.launch

class HomeFragment : BaseFragment<FragmentHomeBinding>(FragmentHomeBinding::inflate) {
    private val smsAdapter = HomeSmsMessageAdapter()
    private val smsRepository by lazy { SmsRepository(requireContext()) }

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
        binding.edtSearchHome.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.grey))

        binding.ivMenu.setOnClickListener {
            (activity as? MainActivity)?.openDrawer()
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
    }

    override fun onResume() {
        super.onResume()
        reloadMessagesOnce()
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
                        smsAdapter.submitList(messages) {
                            updateNoDataState(isLoading = false)
                        }
                    }
            }
        }
    }

    private fun reloadMessagesOnce() {
        viewLifecycleOwner.lifecycleScope.launch {
            val messages = smsRepository.getLatestMessagesByThread()
            smsAdapter.submitList(messages) {
                updateNoDataState(isLoading = false)
            }
        }
    }

    private fun updateNoDataState(isLoading: Boolean) {
        val isEmpty = smsAdapter.itemCount == 0
        binding.layoutNoData.root.isVisible = isLoading || isEmpty
        binding.layoutNoData.prLoading.isVisible = isLoading
        binding.layoutNoData.llNoData.isVisible = !isLoading && isEmpty
        binding.rvMessages.isVisible = !isLoading && !isEmpty
    }
}
