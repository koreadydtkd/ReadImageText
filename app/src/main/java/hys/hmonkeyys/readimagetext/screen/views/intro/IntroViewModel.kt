package hys.hmonkeyys.readimagetext.screen.views.intro

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import hys.hmonkeyys.readimagetext.data.repository.history.HistoryRepository
import hys.hmonkeyys.readimagetext.utils.Utility.getDateWeeksAgo
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class IntroViewModel(
    private val historyRepository: HistoryRepository
) : BaseViewModel() {

    private var _introStateLiveData = MutableLiveData<IntroState>()
    val introLiveData: LiveData<IntroState> = _introStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _introStateLiveData.postValue(IntroState.CheckPermission)
    }

    /** 7일 지난 방문기록 데이터 삭제 */
    fun deleteDataOlderThanOneWeek() = viewModelScope.launch {
        historyRepository.deleteDataOneWeeksAgo(getDateWeeksAgo(1))
    }

    /** 업데이트 버전 체크
     * IntroState.NeedUpdate        - 업데이트 필요
     * IntroState.NoUpdateRequired  - 업데이트 필요 없음
     * */
    fun checkUpdateVersion(currentVersion: Long) {
        val remoteConfig = Firebase.remoteConfig
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    if (remoteConfig.getLong(REMOTE_CONFIG_KEY) > currentVersion) {
                        _introStateLiveData.postValue(IntroState.NeedUpdate)
                    } else {
                        _introStateLiveData.postValue(IntroState.NoUpdateRequired)
                    }
                } else {
                    _introStateLiveData.postValue(IntroState.NoUpdateRequired)
                }
            }
            .addOnFailureListener {
                _introStateLiveData.postValue(IntroState.NoUpdateRequired)
            }
    }

    companion object {
//        private const val TAG = "HYS_IntroViewModel"
        private const val REMOTE_CONFIG_KEY = "app_version"
    }
}