package hys.hmonkeyys.readimagetext.data.api

import hys.hmonkeyys.readimagetext.data.url.KakaoUrl
import hys.hmonkeyys.readimagetext.data.response.KakaoTranslationResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface KakaoApiService {

    @FormUrlEncoded
    @POST(KakaoUrl.GET_TRANSLATE)
    suspend fun translateKakao(
        @Field("query") query: String,
        @Field("src_lang") src_lang: String,
        @Field("target_lang") target_lang: String,
    ): Response<KakaoTranslationResponse>

}