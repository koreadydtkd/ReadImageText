package hys.hmonkeyys.readimagetext.screen.views.main.bottomsheetdialog

import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseBottomSheetDialogFragment
import hys.hmonkeyys.readimagetext.utils.Constant.BLANK
import hys.hmonkeyys.readimagetext.utils.Constant.TTS_PITCH
import hys.hmonkeyys.readimagetext.utils.Utility.hideKeyboardAndCursor
import hys.hmonkeyys.readimagetext.utils.Utility.isKorean
import hys.hmonkeyys.readimagetext.utils.Utility.toast
import java.util.*

@AndroidEntryPoint
internal class BottomDialogFragment(
    private val extractionText: String,
) : BaseBottomSheetDialogFragment<BottomDialogViewModel, FragmentBottomDialogBinding>(), TextToSpeech.OnInitListener {

    override val viewModel: BottomDialogViewModel by viewModels()
    override fun getViewBinding(): FragmentBottomDialogBinding = FragmentBottomDialogBinding.inflate(layoutInflater)

    private val tts: TextToSpeech? by lazy { TextToSpeech(requireContext(), this) }

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
                executeTTS(resultEditText.text.toString())
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

        // TTS 초기화
        initTTS()

        // 추출된 문자 setting
        setResultText()
    }

    /** TTS 실행 */
    private fun executeTTS(text: String) {
        tts?.let {
            // 실행중인 경우 return
            if (it.isSpeaking) return

            try {
                it.setSpeechRate(viewModel.getTTSSpeed())
                it.setPitch(TTS_PITCH)
                it.speak(text, TextToSpeech.QUEUE_FLUSH, null, "id1")
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
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

                binding.progressBar.isVisible = true
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

    /** 한번 실행시켜 초기화 되도록 */
    private fun initTTS() {
        tts?.speak("" ,TextToSpeech.QUEUE_FLUSH, null, "id1")
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
        binding.progressBar.isGone = true
    }

    override fun onDestroy() {
        // tts 정지 및 리소스 해제
        tts?.stop()
        tts?.shutdown()

        // 번역 횟수 초기화
        viewModel.translateCountInit()

        super.onDestroy()
    }

    override fun onInit(status: Int) {
        val ttsLang = tts?.setLanguage(Locale.ENGLISH)

        if (status == TextToSpeech.SUCCESS) {
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                toast(requireContext(), getString(R.string.not_support_language))
                return
            }
        } else {
            toast(requireContext(), getString(R.string.tts_init_fail))
        }
    }

    companion object {
//        private const val TAG = "HYS_BottomDialogFragment"
    }
}