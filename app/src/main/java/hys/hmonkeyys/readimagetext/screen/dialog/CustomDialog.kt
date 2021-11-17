package hys.hmonkeyys.readimagetext.screen.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.databinding.DialogCustomBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener

class CustomDialog(
    val dialogClickedListener: () -> Unit,
) : DialogFragment() {

    private lateinit var binding: DialogCustomBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        setTransparentWindowBackground()
        return DialogCustomBinding.inflate(inflater, container, false).also { binding = it }.root
    }

    // view 영역 외 투명처리
    private fun setTransparentWindowBackground() {
        try {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 업데이트 버튼 클릭
        binding.updateButton.setOnDuplicatePreventionClickListener {
            dialogClickedListener()
        }
    }

}