package hys.hmonkeyys.readimagetext.di

import hys.hmonkeyys.readimagetext.BuildConfig
import hys.hmonkeyys.readimagetext.data.network.KakaoApiService
import hys.hmonkeyys.readimagetext.data.url.KakaoUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

fun provideKakaoApiService(retrofit: Retrofit): KakaoApiService {
    return retrofit.create(KakaoApiService::class.java)
}

fun provideKakaoRetrofit(
    okHttpClient: OkHttpClient,
    gsonConverterFactory: GsonConverterFactory,
): Retrofit {
    return Retrofit.Builder()
        .baseUrl(KakaoUrl.BASE_URL)
        .addConverterFactory(gsonConverterFactory)
        .client(okHttpClient)
        .build()
}

fun provideGsonConvertFactory(): GsonConverterFactory {
    return GsonConverterFactory.create()
}

fun buildOkHttpClient(): OkHttpClient {
    val headerInterceptor = Interceptor {
        val request = it.request()
            .newBuilder()
            .addHeader("Authorization", "KakaoAK ${BuildConfig.KAKAO_API_KEY}")
            .build()
        return@Interceptor it.proceed(request)
    }

    val httpLoggingInterceptor = HttpLoggingInterceptor()
    httpLoggingInterceptor.level = if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
    } else {
        HttpLoggingInterceptor.Level.NONE
    }

    return OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .addInterceptor(headerInterceptor)
        .addInterceptor(httpLoggingInterceptor)
        .build()
}
