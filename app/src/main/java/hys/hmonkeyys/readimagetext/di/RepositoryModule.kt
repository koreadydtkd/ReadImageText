package hys.hmonkeyys.readimagetext.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import hys.hmonkeyys.readimagetext.data.api.KakaoApiService
import hys.hmonkeyys.readimagetext.data.db.dao.HistoryDao
import hys.hmonkeyys.readimagetext.data.db.dao.NoteDao
import hys.hmonkeyys.readimagetext.data.repository.history.DefaultHistoryRepository
import hys.hmonkeyys.readimagetext.data.repository.history.HistoryRepository
import hys.hmonkeyys.readimagetext.data.repository.note.DefaultNoteRepository
import hys.hmonkeyys.readimagetext.data.repository.note.NoteRepository
import hys.hmonkeyys.readimagetext.data.repository.translate.DefaultTranslateRepository
import hys.hmonkeyys.readimagetext.data.repository.translate.TranslateRepository
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideHistoryRepository(
        historyDao: HistoryDao,
        ioDispatcher: CoroutineDispatcher
    ) : HistoryRepository {
        return DefaultHistoryRepository(historyDao, ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideNoteRepository(
        noteDao: NoteDao,
        ioDispatcher: CoroutineDispatcher
    ) : NoteRepository {
        return DefaultNoteRepository(noteDao, ioDispatcher)
    }

    @Singleton
    @Provides
    fun provideKakaoRepository(
        kakaoApiService: KakaoApiService,
        ioDispatcher: CoroutineDispatcher
    ) : TranslateRepository {
        return DefaultTranslateRepository(kakaoApiService, ioDispatcher)
    }


}

