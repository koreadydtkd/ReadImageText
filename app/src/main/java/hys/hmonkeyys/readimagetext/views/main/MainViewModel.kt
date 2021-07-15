package hys.hmonkeyys.readimagetext.views.main

import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import hys.hmonkeyys.readimagetext.data.entity.WebHistoryEntity
import hys.hmonkeyys.readimagetext.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.views.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.IOException

internal class MainViewModel(
    private val historyDao: HistoryDao,
    private val sharedPreferences: SharedPreferences
) : BaseViewModel() {

    private var _mainStateLiveData = MutableLiveData<MainState>()
    val mainStateLiveData: LiveData<MainState> = _mainStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _mainStateLiveData.postValue(MainState.Initialized)
    }

    fun insertAfterDuplicateDataLookup(url: String?) = viewModelScope.launch {
        // 중복 확인을 위한 쿼리 : 0 반환 시 데이터 없음
        val haveData = historyDao.findByHistory(url ?: "", Util().getCurrentDate())
        if(haveData < 1) {
            historyDao.insertHistory(WebHistoryEntity(null, url, Util().getCurrentDate()))
        }
    }

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
                    if(it.text.length > OCR_TEXT_LIMIT) {
                        // 최대 추출 제한
                        _mainStateLiveData.postValue(MainState.TextExtractionComplete(Util.TEXT_LIMIT_EXCEEDED))
                    } else {
                        // 성공
                        _mainStateLiveData.postValue(MainState.TextExtractionComplete(it.text))
                    }
                }
                .addOnFailureListener {
                    it.printStackTrace()
                    FirebaseCrashlytics.getInstance().recordException(it)
                    _mainStateLiveData.postValue(MainState.TextExtractionComplete(Util.EXTRACTION_ERROR))
                }

        } catch (e: IOException) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            _mainStateLiveData.postValue(MainState.TextExtractionComplete(Util.EXTRACTION_ERROR))
        }
    }

    fun getLastUrl(): String = sharedPreferences.getString(SharedPreferencesConst.LAST_URL, getSettingUrl()) ?: DEFAULT_URL

    fun getSettingUrl(): String = sharedPreferences.getString(SharedPreferencesConst.SETTING_URL, DEFAULT_URL) ?: DEFAULT_URL

    fun setLastUrl(url: String) {
        sharedPreferences.edit().putString(SharedPreferencesConst.LAST_URL, url).apply()
    }

    companion object {
        private const val OCR_TEXT_LIMIT = 350

        private const val DEFAULT_URL = "https://www.google.com"
    }
}