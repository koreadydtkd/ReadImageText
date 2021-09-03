package hys.hmonkeyys.readimagetext.utils

import android.os.Handler
import android.os.Looper
import android.view.View

object Expansion {

    /** . ! ? 포함여부 확인 확장함수 */
    fun Char.isSpecialSymbols(): Boolean {
        if (this.toString() == "." || this.toString() == "!" || this.toString() == "?") {
            return true
        }
        return false
    }

    /** 두번 클릭 방지 */
    fun View.setOnDuplicatePreventionClickListener(OnDuplicatePreventionClick: () -> Unit) {
        this.setOnClickListener {
            it.isEnabled = false
            OnDuplicatePreventionClick()
            Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 500)
        }
    }

}