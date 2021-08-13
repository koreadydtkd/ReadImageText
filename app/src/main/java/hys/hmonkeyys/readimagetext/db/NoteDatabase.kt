package hys.hmonkeyys.readimagetext.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hys.hmonkeyys.readimagetext.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.model.entity.Note

// db 구조가 바뀔 때(Column 이 추가 되거나 이름이 바뀌거나) Version 올려줘야함
@Database(entities = [Note::class], version = 1, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        const val DB_NAME = "note-database"
    }
}
