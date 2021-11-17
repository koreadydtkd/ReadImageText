package hys.hmonkeyys.readimagetext.extensions

import android.os.Handler
import android.os.Looper
import android.view.View

/** 두번 클릭 방지 */
fun View.setOnDuplicatePreventionClickListener(OnDuplicatePreventionClick: () -> Unit) {
    this.setOnClickListener {
        it.isEnabled = false
        OnDuplicatePreventionClick()
        Handler(Looper.getMainLooper()).postDelayed({ it.isEnabled = true }, 300)
    }
}