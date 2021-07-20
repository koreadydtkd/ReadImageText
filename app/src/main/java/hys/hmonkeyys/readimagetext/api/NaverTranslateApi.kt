package hys.hmonkeyys.readimagetext.api

import hys.hmonkeyys.readimagetext.data.model.ResultTransferPapago
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface NaverTranslateApi {
    @FormUrlEncoded
    @POST("v1/papago/n2mt")
    fun transferPapago(
        @Field("source") source: String,
        @Field("target") target: String,
        @Field("text") text: String,
    ): Call<ResultTransferPapago>

    companion object {
        private const val CLIENT_ID = "hkbkmkcgTqjpWom1YIuz"
        private const val CLIENT_SECRET = "mpNSK85f7n"
        private const val PAPAGO_API_URL = "https://openapi.naver.com/"

        fun create(): NaverTranslateApi {
            val httpLoggingInterceptor = HttpLoggingInterceptor()
            httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            val headerInterceptor = Interceptor {
                val request = it.request()
                    .newBuilder()
                    .addHeader("X-Naver-Client-Id", CLIENT_ID)
                    .addHeader("X-Naver-Client-Secret", CLIENT_SECRET)
                    .build()
                return@Interceptor it.proceed(request)
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(headerInterceptor)
                .addInterceptor(httpLoggingInterceptor)
                .build()

            return Retrofit.Builder()
                .baseUrl(PAPAGO_API_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NaverTranslateApi::class.java)
        }
    }
}