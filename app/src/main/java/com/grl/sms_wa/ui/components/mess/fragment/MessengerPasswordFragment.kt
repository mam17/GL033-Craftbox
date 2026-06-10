package com.grl.sms_wa.ui.components.mess.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentMessengerPasswordBinding
import com.grl.sms_wa.ui.components.mess.MessengerViewModel

class MessengerPasswordFragment :
    BaseFragment<FragmentMessengerPasswordBinding>(FragmentMessengerPasswordBinding::inflate) {

    private val viewModel: MessengerViewModel by activityViewModels()
    private var firstPin: String? = null
    private var mode: String = MODE_SET

    override fun initView() {
        readArguments()
        binding.ivClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        bindModeText()
        setupPinInputs()
    }

    private fun setupPinInputs() {
        val edits = listOf(binding.edtPin1, binding.edtPin2, binding.edtPin3, binding.edtPin4)

        edits.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (index < edits.size - 1) {
                            edits[index + 1].requestFocus()
                        } else {
                            handlePinComplete()
                        }
                    }
                }
            })

            editText.setOnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    if (editText.text.isEmpty() && index > 0) {
                        edits[index - 1].requestFocus()
                        edits[index - 1].text.clear()
                        return@setOnKeyListener true
                    }
                }
                false
            }
        }
    }

    private fun handlePinComplete() {
        val pin = binding.edtPin1.text.toString() +
                binding.edtPin2.text.toString() +
                binding.edtPin3.text.toString() +
                binding.edtPin4.text.toString()

        when (mode) {
            MODE_REMOVE -> handleRemovePassword(pin)
            MODE_CONFIRM_SET -> handleConfirmSetPassword(pin)
            else -> {
                val confirmFragment = newInstance(mode = MODE_CONFIRM_SET, firstPin = pin)
                addFragment(R.id.frMessDetail, confirmFragment, backStack = "password_confirm")
            }
        }
    }

    private fun handleConfirmSetPassword(pin: String) {
        if (pin == firstPin) {
            viewModel.setConversationPassword(pin)
            showToast(getString(R.string.txt_password_set_successfully))
            parentFragmentManager.popBackStack(
                "password_set",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        } else {
            showToast(getString(R.string.txt_passwords_do_not_match))
            clearInputs()
        }
    }

    private fun handleRemovePassword(pin: String) {
        if (viewModel.isConversationPassword(pin)) {
            viewModel.removeConversationPassword()
            showToast(getString(R.string.txt_password_removed_successfully))
            parentFragmentManager.popBackStack(
                "password_set",
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        } else {
            showToast(getString(R.string.txt_wrong_password_try_again))
            clearInputs()
        }
    }

    private fun bindModeText() {
        when (mode) {
            MODE_REMOVE -> {
                binding.tvTitle.text = getString(R.string.txt_remove_password)
                binding.tvDesc.text = getString(R.string.txt_remove_password_desc)
            }
            MODE_CONFIRM_SET -> {
                binding.tvTitle.text = getString(R.string.txt_confirm_password)
                binding.tvDesc.text = getString(R.string.txt_please_enter_pin)
            }
            else -> {
                binding.tvTitle.text = getString(R.string.txt_set_password)
                binding.tvDesc.text = getString(R.string.txt_please_enter_pin)
            }
        }
    }

    private fun clearInputs() {
        binding.edtPin1.text.clear()
        binding.edtPin2.text.clear()
        binding.edtPin3.text.clear()
        binding.edtPin4.text.clear()
        binding.edtPin1.requestFocus()
    }

    override fun initData() {
    }

    private fun readArguments() {
        firstPin = arguments?.getString(EXTRA_FIRST_PIN)
        mode = arguments?.getString(EXTRA_MODE) ?: MODE_SET
    }

    companion object {
        const val EXTRA_FIRST_PIN = "extra_first_pin"
        const val EXTRA_MODE = "extra_mode"
        const val MODE_SET = "mode_set"
        const val MODE_CONFIRM_SET = "mode_confirm_set"
        const val MODE_REMOVE = "mode_remove"

        fun newInstance(
            mode: String = MODE_SET,
            firstPin: String? = null
        ): MessengerPasswordFragment {
            val fragment = MessengerPasswordFragment()
            val args = Bundle()
            args.putString(EXTRA_MODE, mode)
            args.putString(EXTRA_FIRST_PIN, firstPin)
            fragment.arguments = args
            return fragment
        }
    }
}
