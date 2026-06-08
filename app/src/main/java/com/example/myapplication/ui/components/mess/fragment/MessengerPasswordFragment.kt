package com.example.myapplication.ui.components.mess.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import androidx.fragment.app.FragmentManager
import com.example.myapplication.R
import com.example.myapplication.base.fragment.BaseFragment
import com.example.myapplication.databinding.FragmentMessengerPasswordBinding

class MessengerPasswordFragment :
    BaseFragment<FragmentMessengerPasswordBinding>(FragmentMessengerPasswordBinding::inflate) {

    private var firstPin: String? = null

    override fun initView() {
        binding.ivClose.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        if (firstPin != null) {
            binding.tvTitle.text = getString(R.string.txt_confirm_password)
        } else {
            binding.tvTitle.text = getString(R.string.txt_set_password)
        }

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

        if (firstPin == null) {
            // Move to confirm screen
            val confirmFragment = newInstance(pin)
            addFragment(R.id.frMessDetail, confirmFragment, backStack = "password_confirm")
        } else {
            if (pin == firstPin) {
                showToast(getString(R.string.txt_password_set_successfully))
                // Pop back to detail screen (pop twice)
                parentFragmentManager.popBackStack("password_set", FragmentManager.POP_BACK_STACK_INCLUSIVE)
            } else {
                showToast(getString(R.string.txt_passwords_do_not_match))
                clearInputs()
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
        firstPin = arguments?.getString(EXTRA_FIRST_PIN)
    }

    companion object {
        const val EXTRA_FIRST_PIN = "extra_first_pin"

        fun newInstance(firstPin: String? = null): MessengerPasswordFragment {
            val fragment = MessengerPasswordFragment()
            val args = Bundle()
            args.putString(EXTRA_FIRST_PIN, firstPin)
            fragment.arguments = args
            return fragment
        }
    }
}