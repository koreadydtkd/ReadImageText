package hys.hmonkeyys.readimagetext.data.repository.history

import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory

interface HistoryRepository {

    suspend fun getHistories(): MutableList<WebHistory>

    suspend fun duplicateLookup(loadUrl: String, visitDate: String): Int

    suspend fun insertHistory(webHistory: WebHistory)

    suspend fun deleteDataOneWeeksAgo(oneWeeksAgo: String)

    suspend fun deleteDataTwoWeeksAgo(twoWeeksAgo: String)

    suspend fun deleteSelectedItem(uid: Int, loadUrl: String)

    suspend fun deleteAll()
}