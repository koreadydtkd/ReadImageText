package hys.hmonkeyys.readimagetext.data.repository.translate

import hys.hmonkeyys.readimagetext.data.network.KakaoApiService
import hys.hmonkeyys.readimagetext.model.KakaoTranslationResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DefaultTranslateRepository(
    private val kakaoApiService: KakaoApiService,
    private val ioDispatcher: CoroutineDispatcher
): TranslateRepository {

    override suspend fun getTranslateResult(
        query: String,
        src_lang: String,
        target_lang: String
    ): KakaoTranslationResponse? = withContext(ioDispatcher) {
        val response = kakaoApiService.translateKakao(query, src_lang, target_lang)

        if (response.isSuccessful) {
            response.body()
        } else {
            null
        }
    }
}

