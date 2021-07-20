package hys.hmonkeyys.readimagetext.views.intro

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import hys.hmonkeyys.readimagetext.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.utils.Util
import hys.hmonkeyys.readimagetext.views.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class IntroViewModel(
    private val historyDao: HistoryDao,
) : BaseViewModel() {

    private var _introStateLiveData = MutableLiveData<IntroState>(IntroState.Initialized)
    val introLiveData: LiveData<IntroState> = _introStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _introStateLiveData.postValue(IntroState.CheckPermission)
    }

    fun deleteData() {
        viewModelScope.launch {
            historyDao.deleteDataOneWeeksAgo(Util().getDateWeeksAgo(1))
        }.invokeOnCompletion {
            Log.i(TAG, "7일이 지난 데이터 삭제 완료")
        }
    }

    fun checkUpdateVersion(currentVersion: Long) {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    val updateVersion = remoteConfig.getLong(REMOTE_CONFIG_KEY)
                    Log.i(TAG, "$updateVersion")

                    if (updateVersion > currentVersion) {
                        _introStateLiveData.postValue(IntroState.NeedUpdate(true))
                    } else {
                        _introStateLiveData.postValue(IntroState.NeedUpdate(false))
                    }
                } else {
                    _introStateLiveData.postValue(IntroState.NeedUpdate(false))
                }
            }.addOnFailureListener {
                _introStateLiveData.postValue(IntroState.NeedUpdate(false))
            }
    }

    companion object {
        private const val TAG = "HYS_IntroViewModel"

        private const val REMOTE_CONFIG_KEY = "app_version"
    }
}