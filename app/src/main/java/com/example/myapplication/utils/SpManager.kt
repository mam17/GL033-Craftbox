package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.myapplication.domain.layer.ThemeMessModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val gson = Gson()

    companion object {
        private const val PREF_NAME = "app_prefs"

        fun get(context: Context): SpManager {
            val entryPoint = dagger.hilt.android.EntryPointAccessors.fromApplication(
                context.applicationContext,
                SpEntryPoint::class.java
            )
            return entryPoint.getSpManager()
        }
    }

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface SpEntryPoint {
        fun getSpManager(): SpManager
    }

    // String
    fun putString(key: String, value: String?) {
        prefs.edit { putString(key, value) }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return prefs.getString(key, defaultValue)
    }

    // Boolean
    fun putBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    // Int
    fun putInt(key: String, value: Int) {
        prefs.edit { putInt(key, value) }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return prefs.getInt(key, defaultValue)
    }

    // Float
    fun putFloat(key: String, value: Float) {
        prefs.edit { putFloat(key, value) }
    }

    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return prefs.getFloat(key, defaultValue)
    }

    // Double
    fun putDouble(key: String, value: Double) {
        prefs.edit { putLong(key, java.lang.Double.doubleToRawLongBits(value)) }
    }

    fun getDouble(key: String, defaultValue: Double = 0.0): Double {
        return java.lang.Double.longBitsToDouble(
            prefs.getLong(
                key,
                java.lang.Double.doubleToRawLongBits(defaultValue)
            )
        )
    }

    // ArrayList (Generic using Gson)
    fun <T> putArrayList(key: String, list: ArrayList<T>?) {
        val json = gson.toJson(list)
        prefs.edit { putString(key, json) }
    }

    inline fun <reified T> getArrayList(key: String): ArrayList<T>? {
        val json = getString(key) ?: return null
        val type = object : TypeToken<ArrayList<T>>() {}.type
        return gson.fromJson(json, type)
    }

    // Generic Object
    fun <T> putObject(key: String, obj: T?) {
        val json = gson.toJson(obj)
        putString(key, json)
    }

    inline fun <reified T> getObject(key: String): T? {
        val json = getString(key) ?: return null
        return gson.fromJson(json, T::class.java)
    }

    var isCompletedOnboarding: Boolean
        get() = getBoolean("is_completed_onboarding", false)
        set(value) = putBoolean("is_completed_onboarding", value)

    private fun normalizePhoneNumber(address: String): String {
        return address.replace(" ", "").replace("-", "").replace("+", "").let {
            if (it.startsWith("84")) "0" + it.substring(2) else it
        }
    }

    fun getMessageBackground(address: String): String? = getString("bg_$address", "")

    fun setConversationPassword(address: String, password: String?) {
        val normalized = normalizePhoneNumber(address)
        putString("password_$normalized", password)
    }

    fun getConversationPassword(address: String): String? {
        val normalized = normalizePhoneNumber(address)
        return getString("password_$normalized")
    }

    fun hasConversationPassword(address: String): Boolean {
        return !getConversationPassword(address).isNullOrBlank()
    }

    fun getPasswordAddresses(): List<String> {
        return prefs.all
            .filter { (key, value) ->
                key.startsWith("password_") && (value as? String).isNullOrBlank().not()
            }
            .map { (key, _) -> key.removePrefix("password_") }
            .sorted()
    }

    fun setBlocked(address: String, isBlocked: Boolean) {
        val normalized = normalizePhoneNumber(address)
        putBoolean("blocked_$normalized", isBlocked)
    }

    fun isBlocked(address: String): Boolean {
        val normalized = normalizePhoneNumber(address)
        return getBoolean("blocked_$normalized", false)
    }

    fun getBlockedAddresses(): List<String> {
        return prefs.all
            .filter { (key, value) -> key.startsWith("blocked_") && value == true }
            .map { (key, _) -> key.removePrefix("blocked_") }
            .sorted()
    }

    fun setArchived(address: String, isArchived: Boolean) {
        val normalized = normalizePhoneNumber(address)
        putBoolean("archived_$normalized", isArchived)
    }

    fun isArchived(address: String): Boolean {
        val normalized = normalizePhoneNumber(address)
        return getBoolean("archived_$normalized", false)
    }

    fun getArchivedAddresses(): List<String> {
        return prefs.all
            .filter { (key, value) -> key.startsWith("archived_") && value == true }
            .map { (key, _) -> key.removePrefix("archived_") }
            .sorted()
    }

    fun setNotificationsEnabled(address: String, enabled: Boolean) {
        putBoolean("notify_$address", enabled)
    }

    fun areNotificationsEnabled(address: String): Boolean = getBoolean("notify_$address", true)

    fun setNotificationPreviewMode(address: String, mode: Int) {
        putInt("notification_preview_mode_$address", mode)
    }

    fun getNotificationPreviewMode(address: String): Int =
        getInt("notification_preview_mode_$address", 0)

    fun addScheduledMessage(message: com.example.myapplication.data.model.ScheduledMessage) {
        val list = getArrayList<com.example.myapplication.data.model.ScheduledMessage>("scheduled_messages") ?: arrayListOf()
        list.add(message)
        putArrayList("scheduled_messages", list)
    }

    fun getScheduledMessages(): ArrayList<com.example.myapplication.data.model.ScheduledMessage> {
        return getArrayList<com.example.myapplication.data.model.ScheduledMessage>("scheduled_messages") ?: arrayListOf()
    }

    fun removeScheduledMessage(id: String) {
        val list = getScheduledMessages()
        list.removeAll { it.id == id }
        putArrayList("scheduled_messages", list)
    }

    fun updateScheduledMessage(message: com.example.myapplication.data.model.ScheduledMessage) {
        val list = getScheduledMessages()
        val index = list.indexOfFirst { it.id == message.id }
        if (index != -1) {
            list[index] = message
        } else {
            list.add(message)
        }
        putArrayList("scheduled_messages", list)
    }

    fun markScheduledMessageSent(id: String) {
        val list = getScheduledMessages()
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            val old = list[index]
            list[index] = old.copy(isSent = true)
            putArrayList("scheduled_messages", list)
        }
    }

    fun saveCurrentTheme(theme: ThemeMessModel) {
        saveObject(Constant.KEY_SP_CURRENT_THEME, theme)
    }

    fun getCurrentTheme(): ThemeMessModel? {
        return getObject<ThemeMessModel>(Constant.KEY_SP_CURRENT_THEME)
    }
    fun <T> saveObject(key: String, value: T) {
        prefs.edit {
            putString(key, Gson().toJson(value))
        }
    }

}
