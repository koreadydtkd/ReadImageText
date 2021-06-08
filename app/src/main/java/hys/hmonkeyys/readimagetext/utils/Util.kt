package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Util(context: Context) {

    private val spf: SharedPreferences by lazy {
        context.getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    // 변역에 필요한 파일 다운로드
    fun downloadGoogleTranslator() {
        val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
        val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
        val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()

        CoroutineScope(Dispatchers.IO).launch {
            if(spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, false).not()) {
                downloadTranslateModel(englishModel)
            }

            if(spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, false).not()) {
                downloadTranslateModel(japaneseModel)
            }

            if(spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false).not()) {
                downloadTranslateModel(koreanModel)
            }

        }
    }

    private fun downloadTranslateModel(translateModel: TranslateRemoteModel) {
        val modelManager = RemoteModelManager.getInstance()

        val conditions = DownloadConditions.Builder()
            .requireCharging()
            .build()

        modelManager.download(translateModel, conditions)
            .addOnSuccessListener {
                when(translateModel.language) {
                    TranslateLanguage.ENGLISH -> {
                        Log.d(TAG, "영어 다운로드 완료")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, true).apply()
                    }
                    TranslateLanguage.JAPANESE -> {
                        Log.d(TAG, "일본어 다운로드 완료")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, true).apply()
                    }
                    TranslateLanguage.KOREAN -> {
                        Log.d(TAG, "한국어 다운로드 완료")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, true).apply()
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.toString())
                FirebaseCrashlytics.getInstance().recordException(it)
            }
    }

    companion object {
        private const val TAG = "HYS_Util"
    }
}