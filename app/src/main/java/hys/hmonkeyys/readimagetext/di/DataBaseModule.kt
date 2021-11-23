package hys.hmonkeyys.readimagetext.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import hys.hmonkeyys.readimagetext.data.db.NoteDatabase
import hys.hmonkeyys.readimagetext.data.db.WebDatabase
import hys.hmonkeyys.readimagetext.data.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.data.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataBaseModule {

    @Singleton
    @Provides
    fun historyDatabase(@ApplicationContext context: Context): WebDatabase =
        Room.databaseBuilder(context, WebDatabase::class.java, WebDatabase.DB_NAME).build()

    @Singleton
    @Provides
    fun historyDAO(database: WebDatabase): HistoryDao = database.historyDao()

    @Singleton
    @Provides
    fun noteDatabase(@ApplicationContext context: Context): NoteDatabase =
        Room.databaseBuilder(context, NoteDatabase::class.java, NoteDatabase.DB_NAME).build()

    @Singleton
    @Provides
    fun noteDAO(database: NoteDatabase): NoteDao = database.noteDao()

    @Singleton
    @Provides
    fun createSharedPreferences(@ApplicationContext context: Context): AppPreferenceManager = AppPreferenceManager(context)

}