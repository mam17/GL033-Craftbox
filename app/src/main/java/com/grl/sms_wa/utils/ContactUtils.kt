package com.grl.sms_wa.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.grl.sms_wa.domain.layer.DirectoryModel

object ContactUtils {

    data class ContactInfo(val name: String, val photo: Bitmap?)

    fun getContactInfo(context: Context, address: String): ContactInfo {
        var contactName = address
        var contactBitmap: Bitmap? = null

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return ContactInfo(contactName, contactBitmap)
        }

        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
        val projection = arrayOf(
            ContactsContract.PhoneLookup.DISPLAY_NAME,
            ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI
        )

        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)
                    val photoUriIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                    
                    if (nameIndex != -1) {
                        contactName = cursor.getString(nameIndex) ?: address
                    }
                    if (photoUriIndex != -1) {
                        val photoUriStr = cursor.getString(photoUriIndex)
                        if (photoUriStr != null) {
                            val photoUri = photoUriStr.toUri()
                            contactBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(context.contentResolver, photoUri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ContactInfo(contactName, contactBitmap)
    }

    fun getAllContacts(context: Context): List<DirectoryModel> {
        val list = mutableListOf<DirectoryModel>()
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return list
        }

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI
        )

        try {
            context.contentResolver.query(
                uri,
                projection,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )?.use { cursor ->
                val nameIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val photoUriIndex =
                    cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)

                while (cursor.moveToNext()) {
                    val name = cursor.getString(nameIndex) ?: ""
                    val number = cursor.getString(numberIndex) ?: ""
                    val photoUriStr = cursor.getString(photoUriIndex)
                    var bitmap: Bitmap? = null

                    if (photoUriStr != null) {
                        try {
                            val photoUri = photoUriStr.toUri()
                            bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source =
                                    ImageDecoder.createSource(context.contentResolver, photoUri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    list.add(DirectoryModel(name, number, bitmap))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
    fun getContactNameFromUri(context: Context, contactUri: Uri): String {
        var name = ""
        val projection = arrayOf(ContactsContract.Contacts.DISPLAY_NAME)
        context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                if (index != -1) {
                    name = cursor.getString(index) ?: ""
                }
            }
        }
        return name
    }

    fun getContactNumberFromUri(context: Context, contactUri: Uri): String {
        var number = ""
        val cursor = context.contentResolver.query(contactUri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
                val hasPhoneIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                if (idIndex != -1 && hasPhoneIndex != -1) {
                    val id = it.getString(idIndex)
                    val hasPhoneNumber = it.getInt(hasPhoneIndex)
                    if (hasPhoneNumber > 0) {
                        val phoneCursor = context.contentResolver.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        phoneCursor?.use { pc ->
                            if (pc.moveToFirst()) {
                                val numberIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                if (numberIndex != -1) {
                                    number = pc.getString(numberIndex) ?: ""
                                }
                            }
                        }
                    }
                }
            }
        }
        return number
    }
    fun getContactPhoto(context: Context, phoneNumber: String): Bitmap? {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            return null
        }

        var contactBitmap: Bitmap? = null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))
        val projection = arrayOf(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)

        try {
            context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val photoUriIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.PHOTO_THUMBNAIL_URI)
                    if (photoUriIndex != -1) {
                        val photoUriStr = cursor.getString(photoUriIndex)
                        if (photoUriStr != null) {
                            val photoUri = photoUriStr.toUri()
                            contactBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(context.contentResolver, photoUri)
                                ImageDecoder.decodeBitmap(source)
                            } else {
                                @Suppress("DEPRECATION")
                                MediaStore.Images.Media.getBitmap(context.contentResolver, photoUri)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return contactBitmap
    }

    fun normalizePhoneNumber(address: String): String {
        val digits = address.filter { it.isDigit() }
        return when {
            digits.startsWith("84") && digits.length > 2 -> "0" + digits.substring(2)
            else -> digits
        }
    }
}
