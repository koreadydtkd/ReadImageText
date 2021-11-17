package hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory
import hys.hmonkeyys.readimagetext.data.repository.history.HistoryRepository
import hys.hmonkeyys.readimagetext.screen.BaseViewModel
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter.DateType
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter.HistoryType
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter.UrlType
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

internal class HistoryViewModel(
    private val historyRepository: HistoryRepository
) : BaseViewModel() {

    private var _historyStateLiveData = MutableLiveData<HistoryState>()
    val historyStateData: LiveData<HistoryState> = _historyStateLiveData

    override fun fetchData(): Job = viewModelScope.launch {
        _historyStateLiveData.postValue(HistoryState.Initialized)
    }

    /** 방문기록 데이터 반환 */
    fun getAllHistory() = viewModelScope.launch {
        val historyList = historyRepository.getHistories()
        convertList(historyList)
    }

    /** 방문기록 데이터 변경 */
    private fun convertList(list: MutableList<WebHistory>) {
        var prefDate: String? = ""
        val historyList = mutableListOf<HistoryType>()

        list.forEach { webHistoryModel ->
            // 날짜 영역
            if (webHistoryModel.visitDate != prefDate) {
                historyList.add(
                    DateType(HistoryType.DATE).apply {
                        date = webHistoryModel.visitDate
                    }
                )
                prefDate = webHistoryModel.visitDate
            }

            // 방문 이력
            historyList.add(
                UrlType(HistoryType.URL).apply {
                    loadUrl = webHistoryModel.loadUrl
                    webHistoryModel.uid?.let {
                        uid = it
                    }
                }
            )
        }

        // 새로 변경한 리스트 반환
        _historyStateLiveData.postValue(HistoryState.GetHistoryData(historyList))
    }

    /** 방문기록 데이터 삭제 */
    fun deleteHistory(uid: Int, loadUrl: String) = viewModelScope.launch {
        if (uid == 0 && loadUrl == ALL) {
            historyRepository.deleteAll()
            _historyStateLiveData.postValue(HistoryState.DeleteAll)
        } else {
            historyRepository.deleteSelectedItem(uid, loadUrl)
            _historyStateLiveData.postValue(HistoryState.Delete)
        }
    }

    companion object {
//        private const val TAG = "HistoryViewModel"
        private const val ALL = "all"
    }
}