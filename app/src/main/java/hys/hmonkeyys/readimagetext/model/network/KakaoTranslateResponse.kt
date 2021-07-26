package hys.hmonkeyys.readimagetext.model.network


import com.google.gson.annotations.SerializedName

data class KakaoTranslateResponse(
    @SerializedName("translated_text")
    val translatedText: List<List<String>>?,
)