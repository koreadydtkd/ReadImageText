package hys.hmonkeyys.readimagetext.views.fragment.bottomsheetdialog

import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.retrofit2.kakao.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.model.network.KakaoTranslateResponse
import hys.hmonkeyys.readimagetext.di.TTS
import hys.hmonkeyys.readimagetext.model.entity.Note
import hys.hmonkeyys.readimagetext.model.network.ResultTransferPapago
import hys.hmonkeyys.readimagetext.retrofit2.RetrofitService
import hys.hmonkeyys.readimagetext.utils.Expansion.isSpecialSymbols
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Utility.BLANK
import hys.hmonkeyys.readimagetext.views.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

internal class BottomDialogViewModel(
    private val noteDao: NoteDao,
    private val sharedPreferences: SharedPreferences,
    private val tts: TTS,
) : BaseViewModel() {

    private var _bottomDialogStateLiveData = MutableLiveData<BottomDialogState>()
    val bottomDialogStateLiveData: LiveData<BottomDialogState> = _bottomDialogStateLiveData

    private var _translateCount = MutableLiveData<Int>()
    val translateCount: LiveData<Int> = _translateCount

    override fun fetchData(): Job = viewModelScope.launch {
        _translateCount.value = 0
    }

    /** 거의 대문자인지 체크 */
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

    /** . ? ! 뒤에 문자는 대문자로 변경 */
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
                return BLANK
            }
            return resultText
        }

    }

    /** 번역 횟수 증가 */
    fun increaseCount() {
        _translateCount.value = _translateCount.value?.plus(1)
    }

    /** 번역 횟수 초기화 */
    fun translateCountInit() {
        tts.textToSpeech.stop()
        _translateCount.value = 0
    }

    /** TTS 실행 */
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

    /** TTS 실행 여부 */
    fun isSpeaking(): Boolean = tts.textToSpeech.isSpeaking

    /** 카카오 번역 */
    fun translateKakao(translateText: String) {
        try {
            val replaceText = translateText.replace("\n", " ")

            RetrofitService.create(KAKAO_API_NAME).translateKakao(replaceText, SRC_LANG, TARGET_LANG)
                .enqueue(object : Callback<KakaoTranslateResponse> {
                    override fun onResponse(call: Call<KakaoTranslateResponse>, response: Response<KakaoTranslateResponse>) {
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

                    override fun onFailure(call: Call<KakaoTranslateResponse>, t: Throwable) {
                        FirebaseCrashlytics.getInstance().recordException(t)
                        _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(false))
                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(false))
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /** 네이버 번역 api 추후 사용 시 수정 필요 */
    fun translateNaver(translateText: String) {
        try {
            val replaceText = translateText.replace("\n", " ")

            RetrofitService.create(NAVER_API_NAME).translatePapago(replaceText, SRC_LANG, TARGET_LANG)
                .enqueue(object : Callback<ResultTransferPapago> {
                    override fun onResponse(call: Call<ResultTransferPapago>, response: Response<ResultTransferPapago>) {

                    }

                    override fun onFailure(call: Call<ResultTransferPapago>, t: Throwable) {

                    }
                })
        } catch (e: Exception) {
            e.printStackTrace()
            _bottomDialogStateLiveData.postValue(BottomDialogState.TranslateComplete(false))
            FirebaseCrashlytics.getInstance().recordException(e)
        }

    }

    /** 데이터 삽입 */
    fun insertNoteData(english: String, korean: String) = viewModelScope.launch {
        noteDao.insertHistory(Note(null, english, korean))
    }

    companion object {
        private const val TAG = "HYS_BottomDialogViewModel"

        private const val TTS_PITCH = 1.0F
        private const val TTS_SPEECH_RATE = 0.8F

        private const val SRC_LANG = "en"
        private const val TARGET_LANG = "kr"

        private const val KAKAO_API_NAME = "kakao"
        private const val NAVER_API_NAME = "naver"
    }
}