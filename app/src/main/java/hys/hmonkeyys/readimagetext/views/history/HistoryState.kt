package hys.hmonkeyys.readimagetext.views.history

import hys.hmonkeyys.readimagetext.views.history.adapter.HistoryType

sealed class HistoryState {
    object Initialized: HistoryState()

    data class GetHistoryData(
        val historyList: MutableList<HistoryType>
    ): HistoryState()

    data class Delete(
        val isAll: Boolean
    ): HistoryState()
}