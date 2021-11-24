package hys.hmonkeyys.readimagetext.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMainDispatchers(): MainCoroutineDispatcher = Dispatchers.Main

    @Singleton
    @Provides
    fun provideIoDispatchers(): CoroutineDispatcher = Dispatchers.IO

}