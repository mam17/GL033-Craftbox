package com.grl.sms_wa.ui.components.directory

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.grl.sms_wa.data.repository.SmsRepository
import com.grl.sms_wa.domain.layer.DirectoryModel
import com.grl.sms_wa.utils.ContactUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DirectoryViewModel @Inject constructor(
    private val smsRepository: SmsRepository
) : ViewModel() {

    private val _allContacts = MutableLiveData<List<DirectoryModel>>()
    val allContacts: LiveData<List<DirectoryModel>> get() = _allContacts

    private var originalList = listOf<DirectoryModel>()

    fun fetchContacts(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = ContactUtils.getAllContacts(context)
            val conversations = smsRepository.getConversationList(context)

            val merged = contacts.toMutableList()
            val addedNumbers =
                contacts.map { ContactUtils.normalizePhoneNumber(it.strPhone) }.toMutableSet()

            conversations.forEach { conv ->
                val normalized = ContactUtils.normalizePhoneNumber(conv.address)
                if (normalized.isNotEmpty() && !addedNumbers.contains(normalized)) {
                    merged.add(DirectoryModel(conv.address, conv.address, null))
                    addedNumbers.add(normalized)
                }
            }

            originalList = merged
            _allContacts.postValue(merged)
        }
    }

    fun filterContacts(query: String) {
        if (query.isEmpty()) {
            _allContacts.postValue(originalList)
            return
        }

        val lowerCaseQuery = query.lowercase().trim()
        val filtered = originalList.filter {
            it.strName.lowercase().contains(lowerCaseQuery) ||
                    it.strPhone.lowercase().contains(lowerCaseQuery)
        }
        _allContacts.postValue(filtered)
    }
}
