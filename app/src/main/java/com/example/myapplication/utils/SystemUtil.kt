package com.example.myapplication.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import com.example.myapplication.domain.layer.LanguageModel
import com.example.myapplication.R
import java.util.Locale

object SystemUtil {
    private const val KEY_LANGUAGE = "language"

    fun setLocale(context: Context): Context {
        val language = getLanguage(context)
        return updateResources(context, language)
    }

    fun saveLanguage(context: Context, languageModel: LanguageModel) {
        SpManager.get(context).putObject(KEY_LANGUAGE, languageModel)
    }

    fun getLanguage(context: Context): String {
        val model = SpManager.get(context).getObject<LanguageModel>(KEY_LANGUAGE)
        return model?.languageCode ?: "en"
    }

    fun getLanguageModel(context: Context): LanguageModel {
        val model = SpManager.get(context).getObject<LanguageModel>(KEY_LANGUAGE)
        return model ?: LanguageModel("en", R.drawable.ic_lang_en, R.string.txt_english, true)
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale.forLanguageTag(language)
        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)

        val localeList = LocaleList(locale)
        LocaleList.setDefault(localeList)
        config.setLocales(localeList)

        return context.createConfigurationContext(config)
    }
}
