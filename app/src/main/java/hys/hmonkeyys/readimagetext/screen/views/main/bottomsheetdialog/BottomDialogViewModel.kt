package hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.lifecycle.HiltViewModel
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.data.repository.note.DefaultNoteRepository
import hys.hmonkeyys.readimagetext.data.repository.translate.DefaultTranslateRepository
import hys.hmonkeyys.readimagetext.extensions.isSpecialSymbols
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import hys.hmonkeyys.readimagetext.utils.Constant.BLANK
import hys.hmonkeyys.readimagetext.utils.Pattern.ALPHABET_PATTERN
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
internal class BottomDialogViewModel @Inject constructor(
    private val translateRepository: DefaultTranslateRepository,
    private val noteRepository: DefaultNoteRepository,
    private val pref: AppPreferenceManager,
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
        val onlyEnglishText = Regex(ALPHABET_PATTERN).replace(text, "")

        var textUpperCount = 0
        for (i in 0..onlyEnglishText.lastIndex) {
            if (onlyEnglishText[i].isUpperCase()) {
                textUpperCount += 1
            }
        }
        return onlyEnglishText.length - 3 < textUpperCount
    }

    /** . ? ! ~ 뒤에 문자는 대문자로 변경 */
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

    /** 설정한 TTS 속도 가져오기 */
    fun getTTSSpeed(): Float = pref.getTTSSpeed(AppPreferenceManager.TTS_SPEED)

    /** 번역 횟수 초기화 */
    fun translateCountInit() {
        _translateCount.value = 0
    }

    /** 번역노트(Room DB) 저장 */
    fun insertNoteData(english: String, korean: String) = viewModelScope.launch {
        noteRepository.insertNote(Note(null, english, korean))
        _bottomDialogStateLiveData.postValue(BottomDialogState.InsertComplete)
    }

    /** 카카오 번역 */
    fun translate(translateText: String) = viewModelScope.launch {
        try {
            val replaceText = translateText.replace("\n", " ")
            val translateResult = translateRepository.getTranslateResult(replaceText, SRC_LANG, TARGET_LANG)

            translateResult?.translatedText?.get(0)?.let {
                completedTranslate(it)
            } ?: kotlin.run {
                _bottomDialogStateLiveData.postValue(BottomDialogState.TranslationFailed)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            _bottomDialogStateLiveData.postValue(BottomDialogState.TranslationFailed)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    /** 번역완료 */
    private fun completedTranslate(translateList: List<String>) {
        val sb: StringBuilder = StringBuilder()

        translateList.forEach {
            sb.append("$it ")
        }

        // 번역 횟수 1 증가
        _translateCount.value = _translateCount.value?.plus(1)

        // 번역한 데이터 전달
        _bottomDialogStateLiveData.postValue(BottomDialogState.TranslationComplete(sb.toString()))
    }

    companion object {
//        private const val TAG = "HYS_BottomDialogViewModel"

        private const val SRC_LANG = "en"
        private const val TARGET_LANG = "kr"
    }
}