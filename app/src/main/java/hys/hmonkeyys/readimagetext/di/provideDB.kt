package hys.hmonkeyys.readimagetext.di

import android.content.Context
import androidx.room.Room
import hys.hmonkeyys.readimagetext.data.db.NoteDatabase
import hys.hmonkeyys.readimagetext.data.db.WebDatabase

/** history DB 셋팅 */
internal fun historyDB(context: Context): WebDatabase {
    return Room.databaseBuilder(context, WebDatabase::class.java, WebDatabase.DB_NAME).build()
}
internal fun historyDao(database: WebDatabase) = database.historyDao()

/** note DB 셋팅 */
internal fun noteDB(context: Context): NoteDatabase {
    return Room.databaseBuilder(context, NoteDatabase::class.java, NoteDatabase.DB_NAME).build()
}
internal fun noteDao(database: NoteDatabase) = database.noteDao()