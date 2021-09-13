package hys.hmonkeyys.readimagetext.network

import hys.hmonkeyys.readimagetext.BuildConfig
import hys.hmonkeyys.readimagetext.network.models.KakaoTranslationResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create

object Repository {

    private val kakaoApiService: KakaoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(Url.KAKAO_BASE_URL)
            .client(buildOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create()
    }

    private fun buildOkHttpClient(): OkHttpClient {
        val headerInterceptor = Interceptor {
            val request = it.request()
                .newBuilder()
                .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
                .build()
            return@Interceptor it.proceed(request)
        }

        val httpLoggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(httpLoggingInterceptor)
            .build()
    }

    fun translateEnglishToKorean(query: String, srcLang: String, targetLang: String): Call<KakaoTranslationResponse> {
        return kakaoApiService.translateKakao(query, srcLang, targetLang)
    }

}