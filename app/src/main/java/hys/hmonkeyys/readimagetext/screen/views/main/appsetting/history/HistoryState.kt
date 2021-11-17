package hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history

import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.adapter.HistoryType

sealed class HistoryState {
    object Initialized : HistoryState()

    data class GetHistoryData(
        val historyList: MutableList<HistoryType>,
    ) : HistoryState()

    object DeleteAll: HistoryState()

    object Delete: HistoryState()
}