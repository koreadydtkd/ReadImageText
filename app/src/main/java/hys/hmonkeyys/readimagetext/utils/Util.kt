package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat

class Util {

    fun getAppVersion(applicationContext: Context): Long {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            val currentVersionCode = PackageInfoCompat.getLongVersionCode(info)
            Log.i(TAG, "$currentVersionCode")

            currentVersionCode
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