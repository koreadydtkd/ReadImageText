package hys.hmonkeyys.readimagetext.screen.views.main.appsetting

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.screen.BaseViewModel

internal class AppSettingViewModel(
    private val appPreferenceManager: AppPreferenceManager,
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
        val position = when (appPreferenceManager.getTTSSpeed(AppPreferenceManager.TTS_SPEED)) {
            1.2f -> 0
            1.1f -> 1
            1.0f -> 2
            0.9f -> 3
            0.8f -> 4
            0.7f -> 5
            0.6f -> 6
            0.5f -> 7
            else -> 4
        }
        return position
    }

    /** TTS 속도 저장 */
    fun saveTTSSpeed(position: Int) {
        val speed = when (position) {
            0 -> 1.2f
            1 -> 1.1f
            2 -> 1.0f
            3 -> 0.9f
            4 -> 0.8f
            5 -> 0.7f
            6 -> 0.6f
            7 -> 0.5f
            else -> 0.8f
        }
        appPreferenceManager.setTTSSpeed(AppPreferenceManager.TTS_SPEED, speed)
        _appSettingLiveData.postValue(AppSettingState.SpeedChangeComplete)
    }

    companion object {
        //        private const val TAG = "HYS_AppSettingViewModel"
        private const val DEFAULT_URL = "https://www.google.com"
    }
}