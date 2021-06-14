package hys.hmonkeyys.readimagetext.customdialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import hys.hmonkeyys.readimagetext.databinding.DialogCustomBinding

class CustomDialog(

    val dialogClickedListener: (String) -> Unit

) : DialogFragment() {
    private var binding: DialogCustomBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogCustomBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { binding ->
            binding.dialBtn1.setOnClickListener {
                dialogClickedListener(binding.dialBtn1.text.toString())
            }
            binding.dialBtn2.setOnClickListener {
                dialogClickedListener(binding.dialBtn2.text.toString())
            }
            binding.dialBtn3.setOnClickListener {
                dialogClickedListener(binding.dialBtn3.text.toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
}