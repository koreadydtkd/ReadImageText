package hys.hmonkeyys.readimagetext.network.models


import com.google.gson.annotations.SerializedName

data class KakaoTranslationResponse(
    @SerializedName("translated_text")
    val translatedText: List<List<String>>?,
)