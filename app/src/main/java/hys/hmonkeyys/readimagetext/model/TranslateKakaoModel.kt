package hys.hmonkeyys.readimagetext.model


import com.google.gson.annotations.SerializedName

data class TranslateKakaoModel(
    @SerializedName("translated_text")
    val translatedText: List<List<String>>?
)