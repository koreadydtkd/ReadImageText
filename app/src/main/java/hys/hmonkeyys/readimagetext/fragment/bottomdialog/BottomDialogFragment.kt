package hys.hmonkeyys.readimagetext.fragment.bottomdialog

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.api.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.data.model.TranslateKakaoModel
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.fragment.BaseBottomDialogFragment
import hys.hmonkeyys.readimagetext.utils.Util
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

internal class BottomDialogFragment(
    private val readText: String
) : BaseBottomDialogFragment<BottomDialogViewModel>() {

    private var binding: FragmentBottomDialogBinding? = null

    override val viewModel: BottomDialogViewModel by viewModel()

    private var ocrResultText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { binding ->
            initResultText(binding)
            initButtons(binding)
        }
    }

    override fun observeData() {
        viewModel.bottomDialogStateLiveData.observe(this) {
            when(it) {
                is BottomDialogState.TranslateComplete -> {
                    if(it.isSuccess) {
                        binding!!.resultTranslationEditText.setText(it.translateText)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.translate_fail), Toast.LENGTH_SHORT).show()
                    }
                    binding!!.progressBar.visibility = View.GONE
                }
            }
        }

        viewModel.translateCount.observe(this) { count ->
            Log.i(TAG, "카운트: $count")
            if(count == 3) {
                binding!!.translateButton.apply {
                    setBackgroundResource(R.drawable.clicked_background)
                    isEnabled = false
                    isClickable = false
                }
                binding!!.translateTextView.setTextColor(Color.WHITE)

                Toast.makeText(requireContext(), getString(R.string.selected_translate_limit), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun initResultText(binding: FragmentBottomDialogBinding) {
        val replaceText = readText.replace("\n", " ")

        if(viewModel.isAlmostUpperText(readText)) {
            if(viewModel.getDotTextSort(replaceText) == Util.BLANK) {
                Toast.makeText(requireContext(), getString(R.string.no_results), Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                binding.resultEditText.setText(viewModel.getDotTextSort(replaceText))
            }
        } else {
            binding.resultEditText.setText(replaceText)
        }
    }

    private fun initButtons(binding: FragmentBottomDialogBinding) {
        binding.listenButton.setOnClickListener {
            if(viewModel.isSpeaking()) {
                return@setOnClickListener
            }
            viewModel.speakOut(binding.resultEditText.text.toString())
        }

        binding.translateButton.setOnClickListener {
            if(binding.progressBar.isVisible) {
                Toast.makeText(requireContext(), getString(R.string.wait_please), Toast.LENGTH_SHORT).show()
            } else {
                if(ocrResultText != binding.resultEditText.text.toString()) {
                    ocrResultText = binding.resultEditText.text.toString()

                    binding.progressBar.visibility = View.VISIBLE
                    viewModel.translateKakao(ocrResultText)
                } else {
                    Toast.makeText(requireContext(), getString(R.string.no_text_have_been_changed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroy() {
        viewModel.translateCountInit()

        binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_BottomDialogFragment"
    }

}