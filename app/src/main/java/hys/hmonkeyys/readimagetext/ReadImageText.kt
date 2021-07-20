package hys.hmonkeyys.readimagetext

import android.app.Application
import android.content.Intent
import hys.hmonkeyys.readimagetext.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ReadImageText : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@ReadImageText)
            modules(appModule)
        }

    }

}