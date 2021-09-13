package hys.hmonkeyys.readimagetext.network

import hys.hmonkeyys.readimagetext.network.models.KakaoTranslationResponse
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface KakaoApiService {

    @FormUrlEncoded
    @POST("/v2/translation/translate")
    fun translateKakao(
        @Field("query") query: String,
        @Field("src_lang") src_lang: String,
        @Field("target_lang") target_lang: String,
    ): Call<KakaoTranslationResponse>

}