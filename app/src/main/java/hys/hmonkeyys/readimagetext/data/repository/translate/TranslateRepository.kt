package hys.hmonkeyys.readimagetext.data.repository.translate

import hys.hmonkeyys.readimagetext.data.response.KakaoTranslationResponse

interface TranslateRepository {

    suspend fun getTranslateResult(query: String, src_lang: String, target_lang: String): KakaoTranslationResponse?

}