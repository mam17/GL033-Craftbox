package com.grl.sms_wa.ui.components.directory

import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.inputmethod.EditorInfo
import androidx.core.graphics.toColorInt
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.grl.sms_wa.R
import com.grl.sms_wa.base.fragment.BaseFragment
import com.grl.sms_wa.databinding.FragmentDirectoryBinding
import com.grl.sms_wa.domain.layer.DirectoryModel
import com.grl.sms_wa.ui.components.addnew.NewMessengerActivity
import com.grl.sms_wa.ui.main.MainActivity
import com.grl.sms_wa.utils.SpManager
import com.grl.sms_wa.utils.ViewEx.applyThemeFont
import com.grl.sms_wa.utils.ViewEx.tintColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DirectoryFragment :
    BaseFragment<FragmentDirectoryBinding>(FragmentDirectoryBinding::inflate) {
    private val spManager by lazy { SpManager.get(requireContext()) }
    private val viewModel: DirectoryViewModel by viewModels()

    private val mAdapter = DirectoryAdapter()


    override fun initView() {
        binding.rcvDirectory.adapter = mAdapter
        binding.btnBack.setOnClickListener {
            (activity as? MainActivity)?.closeDrawerFeatureFragment(this)
                ?: removeFragment(this)
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.filterContacts(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edtSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val query = v.text.toString().trim()
                if (query.isNotEmpty()) {
                    if (Patterns.PHONE.matcher(query).matches()) {
                        openNewMessenger(
                            DirectoryModel(
                                strName = query,
                                strPhone = query,
                                photo = null
                            )
                        )
                    } else {
                        if (mAdapter.itemCount == 0) {
                            showToast(getString(R.string.txt_no_contact_found))
                        }
                    }
                }
                true
            } else {
                false
            }
        }

        mAdapter.setOnItemClick { model, i ->
            openNewMessenger(model)
        }
    }

    override fun initData() {
        val currentTheme = spManager.getCurrentTheme()
        mAdapter.setTheme(currentTheme)

        currentTheme?.let { theme ->
            binding.root.applyThemeFont(theme.font)
            binding.apply {
                btnBack.tintColor(theme.colMain.toColorInt())
                imgBGSearch.setBgColor(theme.colMain.toColorInt())
            }
        }

        viewModel.fetchContacts(requireContext())
    }

    override fun initObserver() {
        super.initObserver()
        viewModel.allContacts.observe(viewLifecycleOwner) { contacts ->
            mAdapter.setData(contacts)
            binding.llNoData.root.isVisible = contacts.isEmpty()
        }
    }

    private fun openNewMessenger(model: DirectoryModel) {
        startActivity(
            Intent(requireContext(), NewMessengerActivity::class.java).apply {
                putExtra(NewMessengerActivity.EXTRA_ADDRESS, model.strPhone)
                putExtra(NewMessengerActivity.EXTRA_CONTACT_NAME, model.strName)
            }
        )
    }
}
