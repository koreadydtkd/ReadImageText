package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.pm.PackageInfoCompat
import java.text.SimpleDateFormat
import java.util.*

object Utility {
    private const val TAG = "HYS_Util"

    private const val DATE_PATTERN = "yyyy-MM-dd"
    const val MAIN_TO_HISTORY_DEFAULT = "select_url"
    const val TEXT_LIMIT_EXCEEDED = "text_limit_exceeded"
    const val EXTRACTION_ERROR = "extraction_error"
    const val BLANK = "blank"

    /** 앱 버전 코드 */
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

    /** 앱 버전 이름 */
    fun getAppVersionName(applicationContext: Context): String {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            info.versionName
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    /** 현재 시간 반환 */
    fun getCurrentDate(): String {
        val now = System.currentTimeMillis()
        val date = Date(now)
        val sdf = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
        return sdf.format(date)
    }

    /** 7일 전 날짜 반환 */
    fun getDateWeeksAgo(selectWeek: Int): String {
        val week = Calendar.getInstance()
        week.add(Calendar.DATE, (selectWeek * -7))
        return SimpleDateFormat(DATE_PATTERN, Locale.getDefault()).format(week.time)
    }


    /** 키보드, 커서 숨기기 */
    fun hideKeyboardAndCursor(context: Context, view: View) {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0) // 키보드 숨기기
        view.clearFocus() // 커서 숨기기
    }

}