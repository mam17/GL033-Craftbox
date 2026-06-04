package com.example.myapplication.domain.usecase

import com.example.myapplication.R
import com.example.myapplication.domain.layer.LanguageModel
import javax.inject.Inject

class GetListLanguageUseCase @Inject constructor() :
    UseCase<GetListLanguageUseCase.Param, List<LanguageModel>>() {
    open class Param() : UseCase.Param()

    override suspend fun execute(param: Param): List<LanguageModel> = listOf(
        LanguageModel("hi", R.drawable.ic_lang_hi, R.string.txt_hindi),
        LanguageModel("es", R.drawable.ic_lang_es, R.string.txt_spanish),
        LanguageModel("zh", R.drawable.ic_lang_zh, R.string.txt_chinese),
        LanguageModel("pt", R.drawable.ic_lang_pt, R.string.txt_portuguese),
        LanguageModel("de", R.drawable.ic_lang_de, R.string.txt_germany),
        LanguageModel("ru", R.drawable.ic_lang_ru, R.string.txt_russian),
        LanguageModel("fr", R.drawable.ic_lang_fr, R.string.txt_french),
        LanguageModel("en", R.drawable.ic_lang_en, R.string.txt_english),
        LanguageModel("in", R.drawable.ic_lang_in, R.string.txt_english),
    )
}