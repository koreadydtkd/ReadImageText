package hys.hmonkeyys.readimagetext.retrofit2

import hys.hmonkeyys.readimagetext.model.network.KakaoTranslateResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface KakaoTranslateApi {

    @FormUrlEncoded
    @POST("v2/translation/translate")
    fun translateKakao(
        @Field("query") query: String,
        @Field("src_lang") src_lang: String,
        @Field("target_lang") target_lang: String,
    ): Call<KakaoTranslateResponse>

    companion object {
        private const val REST_API_KEY = "2e6b4f725807e0427ecd3a19d2ca6125"
        private const val KAKAO_API_URL = "https://dapi.kakao.com/"

        fun create(): KakaoTranslateApi {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("Authorization", "KakaoAK $REST_API_KEY")
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(KAKAO_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(KakaoTranslateApi::class.java)
        }
    }
}