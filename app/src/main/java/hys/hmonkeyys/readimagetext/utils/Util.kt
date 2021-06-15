package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.os.Build
import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Util {

    fun getAppVersion(applicationContext: Context): Long {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.longVersionCode
            } else {
                info.versionCode
            }
            Log.d(TAG, "${currentVersion.toLong()}")
            currentVersion.toLong()
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        }
    }

    companion object {
        private const val TAG = "HYS_Util"

        const val MAIN_TO_HISTORY_DEFAULT = "select_url"
    }
}