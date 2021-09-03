package hys.hmonkeyys.readimagetext.views.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.databinding.DialogCustomBinding
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener

class CustomDialog(
    val dialogClickedListener: () -> Unit,
) : DialogFragment() {

    private var binding: DialogCustomBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogCustomBinding.inflate(inflater, container, false)

        // view 영역 외 투명처리
        try {
            dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
        }

        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 업데이트 버튼 클릭
        binding?.updateButton?.setOnDuplicatePreventionClickListener {
            dialogClickedListener()
        }
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}