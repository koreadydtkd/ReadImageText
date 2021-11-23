package hys.hmonkeyys.readimagetext.data.repository.history

import hys.hmonkeyys.readimagetext.data.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DefaultHistoryRepository @Inject constructor(
    private val historyDao: HistoryDao,
    private val ioDispatcher: CoroutineDispatcher,
) : HistoryRepository {

    override suspend fun getHistories(): MutableList<WebHistory> = withContext(ioDispatcher) {
        historyDao.getAll()
    }

    override suspend fun duplicateLookup(loadUrl: String, visitDate: String): Int = withContext(ioDispatcher) {
        historyDao.findByHistory(loadUrl, visitDate)
    }

    override suspend fun insertHistory(webHistory: WebHistory) = withContext(ioDispatcher) {
        historyDao.insertHistory(webHistory)
    }

    override suspend fun deleteDataOneWeeksAgo(oneWeeksAgo: String) = withContext(ioDispatcher) {
        historyDao.deleteDataOneWeeksAgo(oneWeeksAgo)
    }

    override suspend fun deleteDataTwoWeeksAgo(twoWeeksAgo: String) = withContext(ioDispatcher) {
        historyDao.deleteDataTwoWeeksAgo(twoWeeksAgo)
    }

    override suspend fun deleteSelectedItem(uid: Int, loadUrl: String) = withContext(ioDispatcher) {
        historyDao.deleteSelectedItem(uid, loadUrl)
    }

    override suspend fun deleteAll() = withContext(ioDispatcher) {
        historyDao.deleteAll()
    }
}