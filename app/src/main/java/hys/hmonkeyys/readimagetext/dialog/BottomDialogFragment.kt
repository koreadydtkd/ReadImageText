package hys.hmonkeyys.readimagetext.dialog

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.crashlytics.FirebaseCrashlytics
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.api.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.model.TranslateKakaoModel
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.isSpecialSymbols
import hys.hmonkeyys.readimagetext.viewmodel.BottomSheetDialogViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BottomDialogFragment(private val readText: String) : BottomSheetDialogFragment(), TextToSpeech.OnInitListener {

    private var binding: FragmentBottomDialogBinding? = null

    private val spf: SharedPreferences by lazy {
        requireContext().getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private val tts: TextToSpeech by lazy {
        TextToSpeech(requireContext(), this)
    }

    private val model: BottomSheetDialogViewModel by activityViewModels()
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
            // 추출한 텍스트 초기화
            initResultText(binding)

            // 뷰-모델 초기화(번역 횟수 감지)
            initViewModel(binding)

            // 버튼 초기화
            initButtons(binding)
        }
    }

    private fun initResultText(binding: FragmentBottomDialogBinding) {
        val replaceText = readText.replace("\n", " ")
        if(isAlmostUpperText()) {
            binding.resultEditText.setText(getDotTextSort(replaceText))
        } else {
            binding.resultEditText.setText(replaceText)
        }
    }

    private fun isAlmostUpperText() : Boolean {
        val onlyEnglishText = Regex("[^A-Za-z]").replace(readText, "")

        var textUpperCount = 0
        for(i in 0..onlyEnglishText.lastIndex) {
            if(onlyEnglishText[i].isUpperCase()) {
                textUpperCount += 1
            }
        }

        return onlyEnglishText.length - 3 < textUpperCount
    }

    private fun initViewModel(binding: FragmentBottomDialogBinding){
        model.translateCount.observe(viewLifecycleOwner, { count ->
            Log.i(TAG, "카운트: $count")
            if(count == 3) {
                binding.translateButton.apply {
                    setBackgroundResource(R.drawable.clicked_background)
                    isEnabled = false
                    isClickable = false
                }
                binding.translateTextView.setTextColor(Color.WHITE)

                Toast.makeText(requireContext(), resources.getString(R.string.selected_translate_limit), Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun initButtons(binding: FragmentBottomDialogBinding) {
        binding.listenButton.setOnClickListener {
            if(tts.isSpeaking) return@setOnClickListener
            speakOut()
        }

        binding.translateButton.setOnClickListener {
            if(binding.progressBar.isVisible) {
                Toast.makeText(requireContext(), resources.getString(R.string.wait_please), Toast.LENGTH_SHORT).show()
            } else {
                if(ocrResultText != binding.resultEditText.text.toString()) {
                    ocrResultText = binding.resultEditText.text.toString()

                    progressBarShow()
                    translateKakao(ocrResultText)
                } else {
                    Toast.makeText(requireContext(), resources.getString(R.string.no_text_have_been_changed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // TTS 초기화
    override fun onInit(status: Int) {
        try {
            if(status == TextToSpeech.SUCCESS) {
                val ttsLang = tts.setLanguage(Locale.ENGLISH)

                if(ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "This Language is not supported")
                } else {
                    speakOut()
                }
            } else {
                Log.e(TAG, "Initialization Failed!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // TTS 실행
    private fun speakOut() {
        val extractedResults = binding?.resultEditText?.text.toString()

        try {
            tts.setSpeechRate(spf.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_SPEECH_RATE))
            tts.setPitch(TTS_PITCH)
            tts.speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            Toast.makeText(requireContext(), resources.getString(R.string.tts_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDotTextSort(resultText: String): String {
        try {
            var result = resultText.substring(0, 1).uppercase() + resultText.substring(1).lowercase()

            val dotList = mutableListOf<Int>()
            for(i in 0..result.lastIndex - 5) {
                if(result[i].isSpecialSymbols()) {
                    dotList.add(i + 2)
                }
            }

            dotList.forEach { dotIndex ->
                Log.e(TAG, dotIndex.toString())
                result = result.substring(0, dotIndex) +
                        result.substring(dotIndex, dotIndex + 1).uppercase() +
                        result.substring(dotIndex + 1)
            }
            return result
        } catch (e: Exception) {
            e.printStackTrace()
            FirebaseCrashlytics.getInstance().recordException(e)
            Toast.makeText(requireContext(), resources.getString(R.string.tts_error), Toast.LENGTH_SHORT).show()
            return resultText
        }

    }

    /*private fun translatePapago(translateText: String) {
        val replaceText = translateText.replace("\n", " ")

        NaverTranslateApi.create().transferPapago("en", "ko", replaceText).enqueue(object : Callback<ResultTransferPapago> {
            override fun onResponse(call: Call<ResultTransferPapago>, response: Response<ResultTransferPapago>) {
                if(response.isSuccessful.not()) {
                    Log.d("PAPAGO", "response fail")
                    return
                }

                response.body()?.let { ResultTransferPapago ->
                    val result = ResultTransferPapago.message.result.translatedText

                    Log.d("PAPAGO", result)
                    binding?.resultTranslationEditText?.setText(result)
                }
            }

            override fun onFailure(call: Call<ResultTransferPapago>, t: Throwable) {
                Log.e("PAPAGO", t.message.toString())
            }
        })
    }*/

    private fun translateKakao(translateText: String) {
        val replaceText = translateText.replace("\n", " ")

        KakaoTranslateApi.create().translateKakao(replaceText, SRC_LANG, TARGET_LANG).enqueue(object : Callback<TranslateKakaoModel> {
            override fun onResponse(call: Call<TranslateKakaoModel>, response: Response<TranslateKakaoModel>) {
                if(response.isSuccessful.not()) {
                    Toast.makeText(requireContext(), resources.getString(R.string.translate_fail), Toast.LENGTH_SHORT).show()
                    progressBarHide()
                    return
                }

                response.body()?.let { translateKakaoModel ->
                    val sb: StringBuilder = StringBuilder()
                    val items = translateKakaoModel.translatedText?.get(0)
                    items?.forEach {
                        sb.append("$it ")
                    }

                    binding?.resultTranslationEditText?.setText(sb.toString())
                    model.increaseCount()
                    progressBarHide()
                }
            }

            override fun onFailure(call: Call<TranslateKakaoModel>, t: Throwable) {
                FirebaseCrashlytics.getInstance().recordException(t)
                Toast.makeText(requireContext(), resources.getString(R.string.translate_fail), Toast.LENGTH_SHORT).show()
                progressBarHide()
            }
        })
    }

    private fun progressBarShow() {
        binding?.let { binding ->
            binding.progressBar.visibility = View.VISIBLE
        }
    }

    private fun progressBarHide() {
        binding?.let { binding ->
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        model.setDefaultValue()
        binding = null

        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_BottomDialogFragment"
        private const val TTS_PITCH = 1.0F
        private const val TTS_SPEECH_RATE = 0.8F

        private const val SRC_LANG = "en"
        private const val TARGET_LANG = "kr"
    }

}