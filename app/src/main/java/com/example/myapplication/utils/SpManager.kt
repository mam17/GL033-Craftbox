package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.core.content.edit

import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpManager @Inject constructor(@ApplicationContext context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
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
        return java.lang.Double.longBitsToDouble(prefs.getLong(key, java.lang.Double.doubleToRawLongBits(defaultValue)))
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
}
