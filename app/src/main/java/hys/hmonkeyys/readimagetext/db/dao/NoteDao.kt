package hys.hmonkeyys.readimagetext.db.dao

import androidx.room.*
import hys.hmonkeyys.readimagetext.model.entity.Note

@Dao
interface NoteDao {

    // 전체 조회 (최신 날짜부터 조회하고 나중에 추가된 것부터)
    @Query("SELECT * FROM note ORDER BY uid DESC")
    suspend fun getAll(): MutableList<Note>

    // 데이터 삽입
    @Insert
    suspend fun insertHistory(note: Note)


    // 데이터 전체 삭제
    @Query("DELETE FROM note")
    suspend fun deleteAll()

    // 데이터 하나만 삭제
    @Delete
    suspend fun delete(note: Note)

    @Query("DELETE FROM note WHERE uid LIKE :uid")
    suspend fun deleteSelectedItem(uid: Int)
}