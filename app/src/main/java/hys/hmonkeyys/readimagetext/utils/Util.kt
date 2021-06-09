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

class Util(mContext: Context) {

    private val spf: SharedPreferences by lazy {
        mContext.getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private val modelManager: RemoteModelManager by lazy {
        RemoteModelManager.getInstance()
    }

    private val conditions: DownloadConditions by lazy {
        DownloadConditions.Builder()
            .requireCharging()
            .requireWifi()
            .build()
    }

    // 변역에 필요한 파일 다운로드
    fun downloadGoogleTranslator() {
        val translateModelList = mutableListOf<String>()
        modelManager.getDownloadedModels(TranslateRemoteModel::class.java)
            .addOnSuccessListener {
                it.forEach { translateRemoteModel ->
                    Log.e(TAG, translateRemoteModel.language)
                    translateModelList.add(translateRemoteModel.language)
                }
                if(!translateModelList.contains("en")) {
                    downloadTranslateModel(TranslateRemoteModel.Builder(TranslateLanguage.ENGLISH).build())
                }
                if(!translateModelList.contains("ja")) {
                    downloadTranslateModel(TranslateRemoteModel.Builder(TranslateLanguage.JAPANESE).build())
                }
                if(!translateModelList.contains("ko")) {
                    downloadTranslateModel(TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build())
                }
            }
            .addOnFailureListener {
//                FirebaseCrashlytics.getInstance().recordException(it)
            }

    }

    private fun downloadTranslateModel(translateModel: TranslateRemoteModel) {
        CoroutineScope(Dispatchers.IO).launch {
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
//                    FirebaseCrashlytics.getInstance().recordException(it)
                }
        }

    }

    companion object {
        private const val TAG = "HYS_Util"
    }
}