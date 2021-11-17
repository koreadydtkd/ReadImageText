package hys.hmonkeyys.readimagetext.data.db.dao

import androidx.room.*
import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory

@Dao
interface HistoryDao {

    // 전체 조회 (최신 날짜부터 조회하고 나중에 추가된 것부터)
    @Query("SELECT * FROM web ORDER BY visit_date DESC, uid DESC")
    suspend fun getAll(): MutableList<WebHistory>

    // 중복 데이터 조회
    @Query("SELECT COUNT(*) FROM web WHERE load_url LIKE :loadUrl AND visit_date LIKE :visitDate")
    suspend fun findByHistory(loadUrl: String, visitDate: String): Int

    // 데이터 삽입
    @Insert
    suspend fun insertHistory(webHistory: WebHistory)

    // 오늘날짜 기준으로 7일이 지난 데이터 삭제
    @Query("DELETE FROM web WHERE CAST(strftime('%s', visit_date) AS integer) < CAST(strftime('%s', :oneWeeksAgo) AS integer)")
    suspend fun deleteDataOneWeeksAgo(oneWeeksAgo: String)

    // 오늘날짜 기준으로 14일이 지난 데이터 삭제
    @Query("DELETE FROM web WHERE CAST(strftime('%s', visit_date) AS integer) < CAST(strftime('%s', :twoWeeksAgo) AS integer)")
    suspend fun deleteDataTwoWeeksAgo(twoWeeksAgo: String)

    // uid, loadUrl 조건에 해당하는 데이터 삭제
    @Query("DELETE FROM web WHERE uid LIKE :uid AND load_url LIKE :loadUrl")
    suspend fun deleteSelectedItem(uid: Int, loadUrl: String)

    // 데이터 전체 삭제
    @Query("DELETE FROM web")
    suspend fun deleteAll()
}