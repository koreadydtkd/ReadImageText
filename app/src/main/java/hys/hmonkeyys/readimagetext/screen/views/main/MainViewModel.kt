package hys.hmonkeyys.readimagetext.screen.views.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.data.repository.history.DefaultHistoryRepository
import hys.hmonkeyys.readimagetext.utils.Utility.getCurrentDate
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import hys.hmonkeyys.readimagetext.utils.Constant.EXTRACTION_ERROR
import hys.hmonkeyys.readimagetext.utils.Constant.TEXT_LIMIT_EXCEEDED
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
internal class MainViewModel @Inject constructor(
    private val historyRepository: DefaultHistoryRepository,
    private val pref: AppPreferenceManager,
) : BaseViewModel() {

    private var _mainStateLiveData = MutableLiveData<MainState>()
    val mainStateLiveData: LiveData<MainState> = _mainStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _mainStateLiveData.postValue(MainState.Initialized)
    }

    /** 중복 확인 후 db 삽입 */
    fun insertAfterDuplicateDataLookup(url: String?) = viewModelScope.launch {
        // 중복 확인 쿼리 -> 0 반환 시 데이터 없음
        val haveData = historyRepository.duplicateLookup(url ?: "", getCurrentDate())
        if (haveData < 1) {
            historyRepository.insertHistory(WebHistory(null, url, getCurrentDate()))
        }
    }

    /** View -> Bitmap 변경 */
    fun getBitmapFromView(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }

    /**
     * 광학문자인식(OCR, Optical Character Recognition)
     * 이미지에서 텍스트 추출
     */
    fun readImageTextBitmap(selectedBitmap: Bitmap) {
        try {
            val image = InputImage.fromBitmap(selectedBitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener {
                    // 최대 추출 제한(350자)
                    if (it.text.length > OCR_TEXT_LIMIT) {
                        _mainStateLiveData.postValue(MainState.TextExtractionComplete(TEXT_LIMIT_EXCEEDED))
                    } else {
                        // 추출 성공
                        _mainStateLiveData.postValue(MainState.TextExtractionComplete(it.text))
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(it)
                    _mainStateLiveData.postValue(MainState.TextExtractionComplete(EXTRACTION_ERROR))
                }

        } catch (e: IOException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            _mainStateLiveData.postValue(MainState.TextExtractionComplete(EXTRACTION_ERROR))
        }
    }

    /** 설정한 페이지 가져오기 */
    fun getSettingUrl(): String = pref.getUrl(AppPreferenceManager.SETTING_URL) ?: DEFAULT_URL

    /** 마지막 방문 페이지 가져오기 */
    fun getLastUrl(): String = pref.getUrl(AppPreferenceManager.LAST_URL) ?: getSettingUrl()

    /** 마지막 방문 페이지 저장하기 */
    fun setLastUrl(url: String) {
        pref.setUrl(AppPreferenceManager.LAST_URL, url)
    }

    companion object {
        private const val OCR_TEXT_LIMIT = 350
        private const val DEFAULT_URL = "https://www.google.com"
    }
}