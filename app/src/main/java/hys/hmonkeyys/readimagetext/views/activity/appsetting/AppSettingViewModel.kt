package hys.hmonkeyys.readimagetext.views.activity.appsetting

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.views.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class AppSettingViewModel(
    private val sharedPreferences: SharedPreferences,
) : BaseViewModel() {

    private var _appSettingLiveData = MutableLiveData<AppSettingState>()
    val appSettingLiveData: LiveData<AppSettingState> = _appSettingLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _appSettingLiveData.postValue(AppSettingState.Initialized)
    }

    fun getDefaultUrl(): String {
        return sharedPreferences.getString(SharedPreferencesConst.SETTING_URL, DEFAULT_URL) ?: DEFAULT_URL
    }

    fun saveDefaultUrl(url: String) {
        sharedPreferences.edit().putString(SharedPreferencesConst.SETTING_URL, url).apply()
        _appSettingLiveData.postValue(AppSettingState.UrlChangeComplete)
    }

    fun getTTsSpeed(): Int {
        var position = 0
        when (sharedPreferences.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_DEFAULT)) {
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

        Log.e(TAG, "++ $speed ++")

        sharedPreferences.edit().putFloat(SharedPreferencesConst.TTS_SPEED, speed).apply()
        _appSettingLiveData.postValue(AppSettingState.SpeedChangeComplete)
    }

    companion object {
        private const val TAG = "HYS_AppSettingViewModel"

        private const val DEFAULT_URL = "https://www.google.com"
        private const val TTS_DEFAULT = 0.8f

    }
}