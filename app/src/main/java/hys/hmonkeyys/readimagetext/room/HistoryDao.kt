package hys.hmonkeyys.readimagetext.room

import androidx.room.*
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

@Dao
interface HistoryDao {

    // 전체 조회(역순으로)
    @Query("SELECT * FROM web ORDER BY uid DESC")
    suspend fun getAll(): List<WebHistoryModel>

    // 중복 데이터 조회
    @Query("SELECT COUNT(*) FROM web WHERE load_url LIKE :loadUrl AND visit_date LIKE :visitDate")
    suspend fun findByHistory(loadUrl: String, visitDate: String): Int


    // 데이터 삽입
    @Insert
    suspend fun insertHistory(webHistory: WebHistoryModel)


    // 데이터 전체 삭제
    @Query("DELETE FROM web")
    suspend fun deleteAll()

    // 오늘날짜 기준으로 14일전 시간이 지난 데이터 삭제
    @Query("DELETE FROM web WHERE visit_date LIKE :twoWeeksAgo")
    suspend fun deleteDataTwoWeeksAgo(twoWeeksAgo: String)

    // 데이터 하나만 삭제
    @Delete
    suspend fun delete(webHistory: WebHistoryModel)
}