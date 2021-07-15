package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.pm.PackageInfoCompat
import java.text.SimpleDateFormat
import java.util.*

class Util {

    fun getAppVersionCode(applicationContext: Context): Long {
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

    fun getAppVersionName(applicationContext: Context): String {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun getCurrentDate(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        return sdf.format(date)
    }

    // 7일 전 날짜 가져오기
    fun getDateWeeksAgo(selectWeek: Int): String {
        val week = Calendar.getInstance()
        week.add(Calendar.DATE, (selectWeek * -7))
        return SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(week.time)
    }

    companion object {
        private const val TAG = "HYS_Util"

        const val DATE_PATTERN = "yyyy-MM-dd"
        const val MAIN_TO_HISTORY_DEFAULT = "select_url"

        const val TEXT_LIMIT_EXCEEDED = "text_limit_exceeded"
        const val EXTRACTION_ERROR = "extraction_error"

        const val BLANK = "blank"
    }
}

// . ! ? 포함여부 확인 확장함수
fun Char.isSpecialSymbols(): Boolean {
    if(this.toString() == "." || this.toString() == "!" || this.toString() == "?") {
        return true
    }
    return false
}

fun View.setOnDuplicatePreventionClickListener(OnDuplicatePreventionClick: () -> Unit) {
    this.setOnClickListener {
        it.isEnabled = false
        OnDuplicatePreventionClick()
        Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 500)
    }
}
