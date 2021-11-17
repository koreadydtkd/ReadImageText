package hys.hmonkeyys.readimagetext.data.repository.translate

import hys.hmonkeyys.readimagetext.model.KakaoTranslationResponse
import retrofit2.http.Field

interface TranslateRepository {

    suspend fun getTranslateResult(query: String, src_lang: String, target_lang: String): KakaoTranslationResponse?

}