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

/** module - inject(주입)될 대상을 정의 */

/**
 * App 기본 모듈 설정
 * @single - 한번만 객체를 생성
 * @factory - 호출 될때마다 객체 생성
 */
internal val appModule = module {

    single { Dispatchers.Main }
    single { Dispatchers.IO }

    // WebDatabase 셋팅
    single { historyDB(androidApplication()) }
    single { historyDao(get()) }

    // NoteDatabase 셋팅
    single { noteDB(androidApplication()) }
    single { noteDao(get()) }

    // sharedPreferences 셋팅
    single { setSharedPreferences(androidApplication()) }

    // TTS - Text to Speech(텍스트 읽어주는 기능) 셋팅
    single { TTS(androidApplication()) }
}

/** ViewModel 모듈 설정 */
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

/** SharedPreferences 셋팅 */
internal fun setSharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
}

/** TTS - Text to Speech(텍스트 읽어주는 기능) 셋팅 */
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
