package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
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
        if(spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, false).not() ||
            spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, false).not() ||
            spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false).not()) {

            val englishModel = TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build()
            val japaneseModel = TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build()
            val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()

            CoroutineScope(Dispatchers.IO).launch {
                downloadTranslateModel(englishModel)
                downloadTranslateModel(japaneseModel)
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
                        Log.e(TAG, "영어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, true).apply()
                    }
                    TranslateLanguage.JAPANESE -> {
                        Log.e(TAG, "일본어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, true).apply()
                    }
                    TranslateLanguage.KOREAN -> {
                        Log.e(TAG, "한국어")
                        spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, true).apply()
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.toString())
            }
    }

    companion object {
        private const val TAG = "Util"
    }
}