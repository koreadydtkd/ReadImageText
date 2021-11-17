package hys.hmonkeyys.readimagetext.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hys.hmonkeyys.readimagetext.data.db.entity.WebHistory
import hys.hmonkeyys.readimagetext.data.db.dao.HistoryDao

// db 구조가 바뀔 때(Column 이 추가 되거나 이름이 바뀌거나) Version 올려줘야함
@Database(entities = [WebHistory::class], version = 1, exportSchema = false)
abstract class WebDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        const val DB_NAME = "web-database"
    }
}
