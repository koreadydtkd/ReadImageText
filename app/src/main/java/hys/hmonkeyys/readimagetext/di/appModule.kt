package hys.hmonkeyys.readimagetext.di

import android.content.Context
import android.content.SharedPreferences
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.room.Room
import hys.hmonkeyys.readimagetext.db.NoteDatabase
import hys.hmonkeyys.readimagetext.db.WebDatabase
import hys.hmonkeyys.readimagetext.views.fragment.bottomsheetdialog.BottomDialogViewModel
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.views.activity.appsetting.AppSettingViewModel
import hys.hmonkeyys.readimagetext.views.activity.history.HistoryViewModel
import hys.hmonkeyys.readimagetext.views.activity.intro.IntroViewModel
import hys.hmonkeyys.readimagetext.views.activity.main.MainViewModel
import hys.hmonkeyys.readimagetext.views.activity.note.NoteViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import java.util.*

internal val appModule = module {

    single { Dispatchers.Main }
    single { Dispatchers.IO }

    // WebDatabase 셋팅
    single { historyDB(androidApplication()) }
    single { historyDao(get()) }

    // NoteDatabase 셋팅
    single { noteDB(androidApplication()) }
    single { noteDao(get()) }

    single { sharedPreferences(androidApplication()) }

    single { TTS(androidApplication()) }
}

internal val viewModelModule = module {
    // Activity
    viewModel { IntroViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { AppSettingViewModel(get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { NoteViewModel(get(), get(), get()) }

    // Fragment
    viewModel { BottomDialogViewModel(get(), get(), get()) }
}

internal fun historyDB(context: Context): WebDatabase {
    return Room.databaseBuilder(context, WebDatabase::class.java, WebDatabase.DB_NAME).build()
}
internal fun historyDao(database: WebDatabase) = database.historyDao()


internal fun noteDB(context: Context): NoteDatabase {
    return Room.databaseBuilder(context, NoteDatabase::class.java, NoteDatabase.DB_NAME).build()
}
internal fun noteDao(database: NoteDatabase) = database.noteDao()


internal fun sharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
}


internal class TTS(context: Context) : TextToSpeech.OnInitListener {
    val textToSpeech = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsLang = textToSpeech.setLanguage(Locale.ENGLISH)

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "This Language is not supported")
            }
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }

    companion object {
        private const val TAG = "HYS_appModule"
    }
}
