package com.grl.sms_wa.sms_helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat

class ContactInfoResolver(private val context: Context) {
    fun resolve(address: String): ContactInfo {
        if (address.isBlank() || !hasReadContactsPermission()) return ContactInfo.EMPTY

        val lookupUri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(address)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_URI
        )

        return runCatching {
            context.contentResolver.query(lookupUri, projection, null, null, null)?.use { cursor ->
                if (!cursor.moveToFirst()) return@use ContactInfo.EMPTY

                val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                val photoIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_URI)
                ContactInfo(
                    name = cursor.getStringOrNull(nameIndex),
                    photoUri = cursor.getStringOrNull(photoIndex)
                )
            } ?: ContactInfo.EMPTY
        }.getOrDefault(ContactInfo.EMPTY)
    }

    private fun hasReadContactsPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun android.database.Cursor.getStringOrNull(index: Int): String? {
        if (index < 0 || isNull(index)) return null
        return getString(index).takeIf { it.isNotBlank() }
    }
}

data class ContactInfo(
    val name: String?,
    val photoUri: String?
) {
    companion object {
        val EMPTY = ContactInfo(name = null, photoUri = null)
    }
}
