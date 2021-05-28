package hys.hmonkeyys.readimagetext

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import java.util.*

class BottomDialogFragment(

    private val readText: String

) : BottomSheetDialogFragment(), TextToSpeech.OnInitListener {

    private var binding: FragmentBottomDialogBinding? = null

    private val tts: TextToSpeech by lazy {
        TextToSpeech(requireContext(), this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentBottomDialogBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.let { binding ->
            // 추출한 글자 보여주기
            binding.resultTextView.text = readText

            // todo 추출한 글자를 파파고 api 를 사용하여 번역된 글자 보여주기
            binding.resultTranslationTextView.text = "추출한 글자 번역 결과 보여주기"

            binding.listenCardView.setOnClickListener {
                if(tts.isSpeaking) {
                    return@setOnClickListener
                }
                speakOut(readText)
            }

        }
    }

    // TTS 실행
    private fun speakOut(speechText: String) {
        try {
            tts.setPitch(TTS_PITCH)
            tts.setSpeechRate(TTS_SPEECH_RATE)
            tts.speak(speechText, TextToSpeech.QUEUE_FLUSH, null, "id1")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), resources.getString(R.string.tts_error), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onInit(status: Int) {
        try {
            if(status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.ENGLISH)

                if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "This Language is not supported")
                } else {
                    if(readText.isNotEmpty()) {
                        speakOut(readText)
                    }
                }
            } else {
                Log.e(TAG, "Initilization Failed!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        tts?.let {
            tts.stop()
            tts.shutdown()
        }

        binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "BottomDialogFragment"
        private const val TTS_PITCH = 1.0F
        private const val TTS_SPEECH_RATE = 1.0F
    }
}