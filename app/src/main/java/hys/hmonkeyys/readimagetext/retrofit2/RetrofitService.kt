package hys.hmonkeyys.readimagetext.retrofit2

import hys.hmonkeyys.readimagetext.model.network.KakaoTranslateResponse
import hys.hmonkeyys.readimagetext.model.network.ResultTransferPapago
import hys.hmonkeyys.readimagetext.retrofit2.kakao.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.retrofit2.naver.NaverTranslateApi
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface RetrofitService {

    /** 카카오 번역 api */
    @FormUrlEncoded
    @POST("v2/translation/translate")
    fun translateKakao(
        @Field("query") query: String,
        @Field("src_lang") src_lang: String,
        @Field("target_lang") target_lang: String,
    ): Call<KakaoTranslateResponse>

    /** 네이버 파파고 번역 api */
    @FormUrlEncoded
    @POST("v1/papago/n2mt")
    fun translatePapago(
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("text") text: String,
    ): Call<ResultTransferPapago>

    companion object {
        private const val KAKAO_REST_API_KEY = "2e6b4f725807e0427ecd3a19d2ca6125"
        private const val KAKAO_API_URL = "https://dapi.kakao.com/"

        private const val PAPAGO_CLIENT_ID = "hkbkmkcgTqjpWom1YIuz"
        private const val PAPAGO_CLIENT_SECRET = "mpNSK85f7n"
        private const val PAPAGO_API_URL = "https://openapi.naver.com/"

        fun create(apiName: String): RetrofitService {
            val translationApiName = "kakao"
            var isKakao = false

            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                if (apiName == translationApiName) {
                    isKakao = true
                    val request = it.request()
                        .newBuilder()
                        .addHeader("Authorization", "KakaoAK $KAKAO_REST_API_KEY")
                        .build()
                    return@Interceptor it.proceed(request)
                } else {
                    val request = it.request()
                        .newBuilder()
                        .addHeader("X-Naver-Client-Id", PAPAGO_CLIENT_ID)
                        .addHeader("X-Naver-Client-Secret", PAPAGO_CLIENT_SECRET)
                        .build()
                    return@Interceptor it.proceed(request)
                }

            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(if (isKakao) KAKAO_API_URL else PAPAGO_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RetrofitService::class.java)
        }
    }
}