package hys.hmonkeyys.readimagetext.views.activity.history

import hys.hmonkeyys.readimagetext.views.activity.history.adapter.HistoryType

sealed class HistoryState {
    object Initialized : HistoryState()

    data class GetHistoryData(
        val historyList: MutableList<HistoryType>,
    ) : HistoryState()

    data class Delete(
        val isAll: Boolean,
    ) : HistoryState()
}