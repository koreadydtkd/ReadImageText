package hys.hmonkeyys.readimagetext

import android.app.Application
import hys.hmonkeyys.readimagetext.di.appModule
import hys.hmonkeyys.readimagetext.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ReadImageText : Application() {
    override fun onCreate() {
        super.onCreate()

        // 의존성 주입
        startKoin {
            androidLogger()
            androidContext(this@ReadImageText)
            modules(appModule + viewModelModule)
        }

    }

}