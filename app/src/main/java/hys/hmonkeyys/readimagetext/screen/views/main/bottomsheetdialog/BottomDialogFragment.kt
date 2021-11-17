package hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseBottomSheetDialogFragment
import hys.hmonkeyys.readimagetext.utils.Constant.BLANK
import hys.hmonkeyys.readimagetext.utils.Utility.hideKeyboardAndCursor
import hys.hmonkeyys.readimagetext.utils.Utility.isKorean
import hys.hmonkeyys.readimagetext.utils.Utility.toast
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class BottomDialogFragment(
    private val extractionText: String,
) : BaseBottomSheetDialogFragment<BottomDialogViewModel, FragmentBottomDialogBinding>() {

    override val viewModel: BottomDialogViewModel by viewModel()
    override fun getViewBinding(): FragmentBottomDialogBinding = FragmentBottomDialogBinding.inflate(layoutInflater)

    private var ocrResultText = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    /** 각 뷰들 초기화 */
    override fun initViews() = with(binding) {
        // 듣기 버튼 (영어 사용자는 한국어 듣기 기능 없음)
        if (isKorean().not()) {
            listenButton.isGone = true
        } else {
            listenButton.setOnDuplicatePreventionClickListener {
                readText()
            }
        }

        // 번역 버튼
        translateButton.setOnDuplicatePreventionClickListener {
            requireActivity().currentFocus?.let { view ->
                hideKeyboardAndCursor(requireContext(), view)
            }
            translationEnglishToKorean()
        }

        // 노트 추가 버튼
        addNoteButton.setOnDuplicatePreventionClickListener {
            addNoteData(resultEditText.text.toString(), resultTranslationEditText.text.toString())
        }

        // 추출된 문자 setting
        setResultText()
    }

    /** 추출된 문자 초기화 */
    private fun setResultText() {
        val replaceText = extractionText.replace("\n", " ")

        // 거의 대문자인 경우 대소문자로 변경 - 대문자로 번역 api 호출 시 오역 발생될 확률이 높음
        if (viewModel.isAlmostUpperText(extractionText)) {
            // 특수문자 뒤 대문자로 변경, 비어있는 경우 종료
            if (viewModel.getDotTextSort(replaceText) == BLANK) {
                toast(requireContext(), getString(R.string.no_results))
                dismiss()
            } else {
                binding.resultEditText.setText(viewModel.getDotTextSort(replaceText))
            }
        } else {
            binding.resultEditText.setText(replaceText)
        }
    }

    override fun observeData() {
        viewModel.bottomDialogStateLiveData.observe(this) {
            when (it) {
                is BottomDialogState.TranslationComplete -> translationSuccess(it.translateText)

                is BottomDialogState.TranslationFailed -> toast(requireContext(), getString(R.string.translate_fail))

                is BottomDialogState.InsertComplete -> toast(requireContext(), getString(R.string.add_note))
            }
        }

        // 번역 횟수 데이터 변화 감지
        viewModel.translateCount.observe(this) { count ->
            // 선택한 영역에 대한 번역 횟수 3회 제한
            if (count == 3) {
                binding.translateButton.apply {
                    setBackgroundResource(R.drawable.clicked_background)
                    isEnabled = false
                    isClickable = false
                }
                binding.translateTextView.setTextColor(Color.WHITE)

                toast(requireContext(), getString(R.string.selected_translate_limit))
            }
        }
    }

    /** 번역 성공 */
    private fun translationSuccess(translation: String) {
        binding.resultTranslationEditText.setText(translation)
        binding.progressBar.visibility = View.GONE
    }

    /** TTS 실행 */
    private fun readText() {
        if (viewModel.isSpeaking()) return
        viewModel.speakOut(binding.resultEditText.text.toString())
    }

    /** 추출한 문자 번역 */
    private fun translationEnglishToKorean() {
        // 통신 중인 경우 toast
        if (binding.progressBar.isVisible) {
            toast(requireContext(), getString(R.string.wait_please))
        } else {
            // 동일한 text 반복 요청 시 toast
            if (ocrResultText != binding.resultEditText.text.toString()) {
                ocrResultText = binding.resultEditText.text.toString()

                binding.progressBar.visibility = View.VISIBLE
                viewModel.translate(ocrResultText)
            } else {
                toast(requireContext(), getString(R.string.no_text_have_been_changed))
            }
        }
    }

    /** 번역 노트 DB에 데이터 추가 */
    private fun addNoteData(englishText: String, koreanText: String) {
        if (englishText.isEmpty() || koreanText.isEmpty()) {
            toast(requireContext(), getString(R.string.empty_text))
            return
        }

        viewModel.insertNoteData(englishText, koreanText)
    }

    override fun onDestroy() {
        // 번역 횟수 초기화
        viewModel.translateCountInit()

        super.onDestroy()
    }

    companion object {
//        private const val TAG = "HYS_BottomDialogFragment"
    }
}