package hys.hmonkeyys.readimagetext.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import hys.hmonkeyys.readimagetext.model.WebHistoryModel

// 업데이트할때 Version 도 올려줘야함
@Database(entities = [WebHistoryModel::class], version = 1, exportSchema = false)
abstract class WebDatabase: RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        private var instance: WebDatabase? = null

        @Synchronized
        fun getInstance(context: Context): WebDatabase? {
            if (instance == null) {
                synchronized(WebDatabase::class){
                    instance = Room.databaseBuilder(context.applicationContext, WebDatabase::class.java, "web-database" )
                        .build()
                }
            }
            return instance
        }
    }
}
