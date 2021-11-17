package hys.hmonkeyys.readimagetext.data.response


import com.google.gson.annotations.SerializedName

data class KakaoTranslationResponse(
    @SerializedName("translated_text")
    val translatedText: List<List<String>>? = null
)