package hys.hmonkeyys.readimagetext.views.fragment.bottomsheetdialog

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.utils.Utility.BLANK
import hys.hmonkeyys.readimagetext.views.BaseBottomSheetDialogFragment
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class BottomDialogFragment(
    private val extractionText: String,
) : BaseBottomSheetDialogFragment<BottomDialogViewModel>() {

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

        initResultText()
        initViews()
    }

    override fun observeData() {
        viewModel.bottomDialogStateLiveData.observe(this) {
            when (it) {
                is BottomDialogState.TranslateComplete -> {
                    if (it.isSuccess) {
                        binding?.resultTranslationEditText?.setText(it.translateText)
                    } else {
                        Toast.makeText(requireContext(), getString(R.string.translate_fail), Toast.LENGTH_SHORT).show()
                    }
                    binding?.progressBar?.visibility = View.GONE
                }
            }
        }

        // 번역 횟수 데이터 변화 감지
        viewModel.translateCount.observe(this) { count ->
            // 선택한 영역에 대한 번역 횟수 3회 제한
            if (count == 3) {
                binding?.translateButton?.apply {
                    setBackgroundResource(R.drawable.clicked_background)
                    isEnabled = false
                    isClickable = false
                }
                binding?.translateTextView?.setTextColor(Color.WHITE)

                Toast.makeText(requireContext(), getString(R.string.selected_translate_limit), Toast.LENGTH_LONG).show()
            }
        }
    }

    /** 추출된 문자 초기화 */
    private fun initResultText() {
        val replaceText = extractionText.replace("\n", " ")

        if (viewModel.isAlmostUpperText(extractionText)) {
            if (viewModel.getDotTextSort(replaceText) == BLANK) {
                Toast.makeText(requireContext(), getString(R.string.no_results), Toast.LENGTH_SHORT).show()
                dismiss()
            } else {
                binding?.resultEditText?.setText(viewModel.getDotTextSort(replaceText))
            }
        } else {
            binding?.resultEditText?.setText(replaceText)
        }
    }

    /** 각 뷰들 초기화 */
    private fun initViews() {
        // 듣기 버튼
        binding?.listenButton?.setOnDuplicatePreventionClickListener {
            readText()
        }

        // 번역 버튼
        binding?.translateButton?.setOnDuplicatePreventionClickListener {
            translationEnglishToKorean()
        }

        // 노트 추가 버튼
        binding?.addNoteButton?.setOnDuplicatePreventionClickListener {
            addNoteData(binding?.resultEditText?.text.toString(), binding?.resultTranslationEditText?.text.toString())

        }
    }

    /** TTS 실행 */
    private fun readText() {
        if (viewModel.isSpeaking()) return
        viewModel.speakOut(binding?.resultEditText?.text.toString())
    }

    /** 추출한 문자 번역 */
    private fun translationEnglishToKorean() {
        if (binding?.progressBar?.isVisible == true) {
            Toast.makeText(requireContext(), getString(R.string.wait_please), Toast.LENGTH_SHORT).show()
        } else {
            if (ocrResultText != binding?.resultEditText?.text.toString()) {
                ocrResultText = binding?.resultEditText?.text.toString()

                binding?.progressBar?.visibility = View.VISIBLE
                viewModel.translate(ocrResultText)
            } else {
                Toast.makeText(requireContext(), getString(R.string.no_text_have_been_changed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 번역 노트 DB에 데이터 추가 */
    private fun addNoteData(englishText: String, koreanText: String) {
        if (englishText.isEmpty() || koreanText.isEmpty()) {
            Toast.makeText(requireContext(), getString(R.string.empty_text), Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.insertNoteData(englishText, koreanText)
        Toast.makeText(requireContext(), getString(R.string.add_note), Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        // 번역 횟수 초기화
        viewModel.translateCountInit()

        binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_BottomDialogFragment"
    }
}