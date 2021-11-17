package hys.hmonkeyys.readimagetext.screen.views.main.appsetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.screen.BaseViewModel

internal class AppSettingViewModel(
    private val appPreferenceManager: AppPreferenceManager
) : BaseViewModel() {

    private var _appSettingLiveData = MutableLiveData<AppSettingState>()
    val appSettingLiveData: LiveData<AppSettingState> = _appSettingLiveData

    /** 셋팅 URL 반환 - 없는 경우 기본 URL 반환 */
    fun getDefaultUrl(): String = appPreferenceManager.getUrl(AppPreferenceManager.SETTING_URL) ?: DEFAULT_URL

    /** 셋팅 URl 저장 */
    fun saveDefaultUrl(url: String) {
        appPreferenceManager.setUrl(AppPreferenceManager.SETTING_URL, url)
        _appSettingLiveData.postValue(AppSettingState.UrlChangeComplete)
    }

    /** TTS 속도 반환 */
    fun getTTsSpeed(): Int {
        var position = 0
        when (appPreferenceManager.getTTSSpeed(AppPreferenceManager.TTS_SPEED)) {
            1.2f -> position = 0
            1.1f -> position = 1
            1.0f -> position = 2
            0.9f -> position = 3
            0.8f -> position = 4
            0.7f -> position = 5
            0.6f -> position = 6
            0.5f -> position = 7
        }
        return position
    }

    /** TTS 속도 저장 */
    fun saveTTSSpeed(position: Int) {
        var speed = 0.8f
        when (position) {
            0 -> speed = 1.2f
            1 -> speed = 1.1f
            2 -> speed = 1.0f
            3 -> speed = 0.9f
            4 -> speed = 0.8f
            5 -> speed = 0.7f
            6 -> speed = 0.6f
            7 -> speed = 0.5f
        }

        appPreferenceManager.setTTSSpeed(AppPreferenceManager.TTS_SPEED, speed)
        _appSettingLiveData.postValue(AppSettingState.SpeedChangeComplete)
    }

    companion object {
//        private const val TAG = "HYS_AppSettingViewModel"
        private const val DEFAULT_URL = "https://www.google.com"
    }
}