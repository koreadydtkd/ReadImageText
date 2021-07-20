package hys.hmonkeyys.readimagetext.fragment.bottomdialog

import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.api.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.data.model.TranslateKakaoModel
import hys.hmonkeyys.readimagetext.di.TTS
import hys.hmonkeyys.readimagetext.fragment.BaseFragmentViewModel
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.utils.isSpecialSymbols
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class BottomDialogViewModel(
    private val sharedPreferences: SharedPreferences,
    private val tts: TTS,
) : BaseFragmentViewModel() {

    // 화면 상태
    private var _bottomDialogStateLiveData = MutableLiveData<BottomDialogState>()
    val bottomDialogStateLiveData: LiveData<BottomDialogState> = _bottomDialogStateLiveData

    // 번역 카운트
    private var _translateCount = MutableLiveData<Int>()
    val translateCount: LiveData<Int> = _translateCount

    override fun fetchData(): Job = viewModelScope.launch {
        _translateCount.value = 0
    }

    fun isAlmostUpperText(text: String): Boolean {
        val onlyEnglishText = Regex("[^A-Za-z]").replace(text, "")

        var textUpperCount = 0
        for (i in 0..onlyEnglishText.lastIndex) {
            if (onlyEnglishText[i].isUpperCase()) {
                textUpperCount += 1
            }
        }
        return onlyEnglishText.length - 3 < textUpperCount
    }

    fun getDotTextSort(resultText: String): String {
        try {
            var result = resultText.substring(0, 1).uppercase() + resultText.substring(1).lowercase()

            val dotList = mutableListOf<Int>()
            for (i in 0..result.lastIndex - 5) {
                if (result[i].isSpecialSymbols()) {
                    dotList.add(i + 2)
                }
            }

            dotList.forEach { dotIndex ->
                Log.e(TAG, dotIndex.toString())
                result = result.substring(0, dotIndex) +
                        result.substring(dotIndex, dotIndex + 1).uppercase() +
                        result.substring(dotIndex + 1)
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            if (resultText.isBlank()) {
                return Util.BLANK
            }
            return resultText
        }

    }

    fun increaseCount() {
        _translateCount.value = _translateCount.value?.plus(1)
    }

    fun translateCountInit() {
        tts.textToSpeech.stop()
        _translateCount.value = 0
    }

    fun speakOut(extractedResults: String) {
        try {
            tts.textToSpeech.apply {
                setSpeechRate(sharedPreferences.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_SPEECH_RATE))
                setPitch(TTS_PITCH)
                speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    fun isSpeaking(): Boolean = tts.textToSpeech.isSpeaking

    fun translateKakao(translateText: String) {
        val replaceText = translateText.replace("\n", " ")

        KakaoTranslateApi.create().translateKakao(replaceText, SRC_LANG, TARGET_LANG).enqueue(object :
            Callback<TranslateKakaoModel> {
            override fun onResponse(call: Call<TranslateKakaoModel>, response: Response<TranslateKakaoModel>) {
                if (response.isSuccessful.not()) {
                    _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(false))
                    return
                }

                response.body()?.let { translateKakaoModel ->
                    val sb: StringBuilder = StringBuilder()
                    val items = translateKakaoModel.translatedText?.get(0)
                    items?.forEach {
                        sb.append("$it ")
                    }
                    _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(true, sb.toString()))
                    increaseCount()
                }
            }

            override fun onFailure(call: Call<TranslateKakaoModel>, t: Throwable) {
                FirebaseCrashlytics.getInstance().recordException(t)
                _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(false))
            }
        })
    }

    companion object {
        private const val TAG = "HYS_BottomDialogViewModel"

        private const val TTS_PITCH = 1.0F
        private const val TTS_SPEECH_RATE = 0.8F

        private const val SRC_LANG = "en"
        private const val TARGET_LANG = "kr"
    }
}