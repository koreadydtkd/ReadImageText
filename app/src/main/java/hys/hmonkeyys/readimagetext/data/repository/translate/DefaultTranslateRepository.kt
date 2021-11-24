package hys.hmonkeyys.readimagetext.data.repository.translate

import hys.hmonkeyys.readimagetext.data.api.KakaoApiService
import hys.hmonkeyys.readimagetext.data.response.KakaoTranslationResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class DefaultTranslateRepository (
    private val kakaoApiService: KakaoApiService,
    private val ioDispatcher: CoroutineDispatcher,
) : TranslateRepository {

    override suspend fun getTranslateResult(
        query: String,
        src_lang: String,
        target_lang: String,
    ): KakaoTranslationResponse? = withContext(ioDispatcher) {

        val response = kakaoApiService.translateKakao(
            query = query,
            src_lang = src_lang,
            target_lang = target_lang
        )

        if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}

