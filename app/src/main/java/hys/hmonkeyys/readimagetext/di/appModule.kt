package hys.hmonkeyys.readimagetext.di

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import hys.hmonkeyys.readimagetext.data.preference.AppPreferenceManager
import hys.hmonkeyys.readimagetext.data.repository.history.DefaultHistoryRepository
import hys.hmonkeyys.readimagetext.data.repository.history.HistoryRepository
import hys.hmonkeyys.readimagetext.data.repository.note.DefaultNoteRepository
import hys.hmonkeyys.readimagetext.data.repository.note.NoteRepository
import hys.hmonkeyys.readimagetext.data.repository.translate.DefaultTranslateRepository
import hys.hmonkeyys.readimagetext.data.repository.translate.TranslateRepository
import hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog.BottomDialogViewModel
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.AppSettingViewModel
import hys.hmonkeyys.readimagetext.screen.views.main.appsetting.history.HistoryViewModel
import hys.hmonkeyys.readimagetext.screen.views.intro.IntroViewModel
import hys.hmonkeyys.readimagetext.screen.views.main.MainViewModel
import hys.hmonkeyys.readimagetext.screen.views.main.note.NoteViewModel
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
    single { AppPreferenceManager(androidApplication()) }

    // retrofit 셋팅
    single { provideGsonConvertFactory() }
    single { buildOkHttpClient() }
    single { provideKakaoRetrofit(get(), get()) }
    single { provideKakaoApiService(get()) }

    // TTS - Text to Speech(텍스트 읽어주는 기능) 셋팅
    single { TTS(androidApplication()) }

    // Repository - Api
    single<TranslateRepository> { DefaultTranslateRepository(get(), get()) }

    // Repository - Room
    single<HistoryRepository> { DefaultHistoryRepository(get(), get()) }
    single<NoteRepository> { DefaultNoteRepository(get(), get()) }
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
    viewModel { BottomDialogViewModel(get(), get(), get(), get()) }
}

/** TTS - Text to Speech(텍스트 읽어주는 기능) 셋팅 */
internal class TTS(context: Context) : TextToSpeech.OnInitListener {
    val textToSpeech = TextToSpeech(context, this)

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsLang = textToSpeech.setLanguage(Locale.ENGLISH)

            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "이 언어는 되지 않음")
            }
        } else {
            Log.e(TAG, "Initialization Failed!")
        }
    }

    companion object {
        private const val TAG = "HYS_appModule"
    }
}
