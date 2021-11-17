package hys.hmonkeyys.readimagetext.utils

import android.content.Context
import android.content.pm.PackageInfo
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import hys.hmonkeyys.readimagetext.utils.Pattern.DATE_PATTERN
import java.text.SimpleDateFormat
import java.util.*

object Utility {

    /** 앱 버전 코드 */
    fun getAppVersionCode(applicationContext: Context): Long {
        return try {
            val info: PackageInfo = applicationContext.packageManager.getPackageInfo(applicationContext.packageName, 0)
            PackageInfoCompat.getLongVersionCode(info)
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

    /** 한국어 언어 설정 여부 */
    fun isKorean(): Boolean = Locale.getDefault().language == "ko"

    /** 짧은 토스트 뛰우기 */
    fun toast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }

    /** 짧은 토스트 뛰우기 */
    fun longToast(context: Context, msg: String) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
    }
}