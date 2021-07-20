package hys.hmonkeyys.readimagetext.db

import androidx.room.Database
import androidx.room.RoomDatabase
import hys.hmonkeyys.readimagetext.data.entity.WebHistoryEntity
import hys.hmonkeyys.readimagetext.db.dao.HistoryDao

// 업데이트할때 Version 도 올려줘야함
@Database(entities = [WebHistoryEntity::class], version = 1, exportSchema = false)
abstract class WebDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        const val DB_NAME = "web-database"

        /*private var instance: WebDatabase? = null

        @Synchronized
        fun getInstance(context: Context): WebDatabase? {
            if (instance == null) {
                synchronized(WebDatabase::class){
                    instance = Room.databaseBuilder(context.applicationContext, WebDatabase::class.java, "web-database" )
                        .build()
                }
            }
            return instance
        }*/
    }
}
