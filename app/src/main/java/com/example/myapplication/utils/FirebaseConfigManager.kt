package com.example.myapplication.utils

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.example.myapplication.BuildConfig
import com.example.myapplication.MyApplication

class FirebaseConfigManager private constructor() {

    companion object {
        private const val TAG = "FirebaseConfigManager"
        private const val MAX_RETRY = 5

        const val KEY_REMOTE_API_BASE_URL = "url_api"
        const val KEY_R2_BASE_URL = "r2_base_url"
        const val KEY_TEMPLATE_COLLECTION = "template_collection"
        const val DEFAULT_R2_BASE_URL = "https://pub-3343da4075314109b88b0f1e099064a5.r2.dev/"
        private const val DEFAULT_TEMPLATE_COLLECTION = "app_catalog"
        private const val KEY_ADS_CONFIG = "ad_config"

        @Volatile
        private var INSTANCE: FirebaseConfigManager? = null

        fun instance(): FirebaseConfigManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseConfigManager().also {
                    INSTANCE = it
                }
            }
        }
    }

    private val remoteConfig: FirebaseRemoteConfig?
        get() = try {
            FirebaseRemoteConfig.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "FirebaseRemoteConfig not initialized: ${e.message}")
            null
        }

    private val gson = Gson()

    private var retryCount = 0
    private var isFetching = false
    private val pendingFetchCallbacks = mutableListOf<(Boolean) -> Unit>()
    var isEnableAllAds: Boolean = true
//    var adConfig: AdConfig = AdConfig()

    init {
        Log.i(TAG, "Initializing FirebaseConfigManager...")
        setup()
//        val sp = MyApplication.instance?.spManager
//        val json = sp?.getString(KEY_ADS_CONFIG, "") ?: ""
//        if (json.isNotEmpty()) {
//            try {
//                adConfig = gson.fromJson(json, AdConfig::class.java)
//            } catch (e: Exception) {
//                Log.e(TAG, "Parse error local ad_config: ${e.message}")
//            }
//        }
    }

    private fun setup() {
        val config = remoteConfig ?: return
        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = if (BuildConfig.DEBUG) 0 else 3600
        }
        config.setConfigSettingsAsync(settings)
        Log.i(TAG, "Firebase RemoteConfig setup complete (DEBUG=${BuildConfig.DEBUG})")
    }

    fun checkAndUpdateIfChanged(): Boolean {
        val context = MyApplication.context ?: return false

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Log.d(TAG, "No internet for RemoteConfig check")
            return false
        }

        val config = remoteConfig ?: return false
        val remoteUrl = config.getString(KEY_REMOTE_API_BASE_URL).trim()
        if (!isValidUrl(remoteUrl)) {
            Log.d(TAG, "Invalid api_url from RemoteConfig")
            return false
        }

        return saveIfChanged(remoteUrl)
    }

    fun fetch(onResult: ((Boolean) -> Unit)? = null) {
        Log.i(TAG, "fetch() called, isFetching=$isFetching")
        onResult?.let { pendingFetchCallbacks.add(it) }
        if (isFetching) return

        val config = remoteConfig
        if (config == null) {
            Log.e(TAG, "RemoteConfig is null - fetch aborted")
            notifyFetchResult(false)
            return
        }

        val context = MyApplication.context
        if (context == null) {
            notifyFetchResult(false)
            return
        }

        if (!NetworkUtil.isNetworkAvailable(context)) {
            Log.e(TAG, "No internet - fetch aborted")
            notifyFetchResult(false)
            return
        }

        isFetching = true

        config.fetchAndActivate().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e(TAG, "Fetch failed: ${task.exception?.message}")
                retry()
                return@addOnCompleteListener
            }

            parseAndSaveAdConfig()

            val remoteUrl = config.getString(KEY_REMOTE_API_BASE_URL).trim()
            if (isValidUrl(remoteUrl)) {
                saveIfChanged(remoteUrl)
            } else {
                Log.d(TAG, "RemoteConfig url_api is empty or invalid; continuing without API base url")
            }

            retryCount = 0
            notifyFetchResult(true)
        }
    }

    private fun retry() {
        retryCount++
        if (retryCount < MAX_RETRY) {
            isFetching = false
            fetch()
        } else {
            Log.d(TAG, "Retry max reached")
            retryCount = 0
            notifyFetchResult(false)
        }
    }

    private fun saveIfChanged(remoteUrl: String): Boolean {
        val context = MyApplication.context ?: return false
        val sp = SpManager.get(context)
        val normalizedRemoteUrl = normalizeUrl(remoteUrl)
        val localUrl = normalizeUrl(sp.getString(KEY_REMOTE_API_BASE_URL, "") ?: "")
        if (normalizedRemoteUrl == localUrl) {
            Log.i(TAG, "BASE_URL unchanged: $normalizedRemoteUrl")
            return false
        }

        sp.putString(KEY_REMOTE_API_BASE_URL, normalizedRemoteUrl)
        Log.i(TAG, "SUCCESS: Saved new BASE_URL = $normalizedRemoteUrl")
        return true
    }

    private fun notifyFetchResult(success: Boolean) {
        isFetching = false
        val callbacks = pendingFetchCallbacks.toList()
        pendingFetchCallbacks.clear()
        callbacks.forEach { it(success) }
    }

    fun getBaseUrl(): String {
        val context = MyApplication.context ?: return ""
        val url = SpManager.get(context)
            .getString(KEY_REMOTE_API_BASE_URL, "")
            ?.trim()
            .orEmpty()
        return normalizeUrl(url)
    }

    fun getR2BaseUrl(): String {
        val remoteUrl = getString(KEY_R2_BASE_URL).trim()
        if (isValidUrl(remoteUrl)) {
            val normalized = normalizeUrl(remoteUrl)
            Log.d(TAG, "getR2BaseUrl: remoteConfig=$normalized")
            return normalized
        }

        val context = MyApplication.context ?: return ""
        val localUrl = SpManager.get(context)
            .getString(KEY_R2_BASE_URL, "")
            ?.trim()
            .orEmpty()
        val normalized = normalizeUrl(localUrl)
        if (normalized.isNotBlank()) {
            Log.d(TAG, "getR2BaseUrl: local=$normalized remoteRaw=$remoteUrl")
            return normalized
        }

        Log.w(TAG, "getR2BaseUrl: Remote Config/local empty, fallback=$DEFAULT_R2_BASE_URL")
        return DEFAULT_R2_BASE_URL
    }

    fun getTemplateCollection(): String {
        val collection = getString(KEY_TEMPLATE_COLLECTION, DEFAULT_TEMPLATE_COLLECTION)
            .ifBlank { DEFAULT_TEMPLATE_COLLECTION }
        Log.d(TAG, "getTemplateCollection: $collection")
        return collection
    }

    fun hasValidBaseUrl(): Boolean = getBaseUrl().isNotEmpty()

    fun getString(key: String, defaultValue: String = ""): String =
        remoteConfig?.getString(key)?.ifEmpty { defaultValue } ?: defaultValue

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean =
        runCatching { remoteConfig?.getBoolean(key) }.getOrElse { defaultValue } ?: defaultValue

    fun getLong(key: String, defaultValue: Long = 0L): Long =
        runCatching { remoteConfig?.getLong(key) }.getOrElse { defaultValue } ?: defaultValue

    private fun isValidUrl(url: String): Boolean =
        url.startsWith("http://") || url.startsWith("https://")

    private fun normalizeUrl(url: String): String {
        if (url.isEmpty()) return ""
        return if (url.endsWith("/")) url else "$url/"
    }

    private fun parseAndSaveAdConfig() {
        saveRemoteStringIfValidUrl(KEY_R2_BASE_URL)
//        val sp = MyApplication.instance?.spManager ?: return
//        val json = remoteConfig.getString(KEY_ADS_CONFIG)
//        val oldJson = sp.getString(KEY_ADS_CONFIG, "") ?: ""
//        if (json.isNotBlank() && json != oldJson) {
//            try {
//                adConfig = gson.fromJson(json, AdConfig::class.java)
//                sp.putString(KEY_ADS_CONFIG, json)
//                Log.d(TAG, "Updated ad_config from remote $adConfig")
//            } catch (e: Exception) {
//                Log.e(TAG, "Parse ad_config error: ${e.message}")
//            }
//        }
    }

    private fun saveRemoteStringIfValidUrl(key: String) {
        val context = MyApplication.context ?: return
        val remoteValue = getString(key).trim()
        if (!isValidUrl(remoteValue)) return
        SpManager.get(context).putString(key, normalizeUrl(remoteValue))
    }

}
