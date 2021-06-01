package hys.hmonkeyys.readimagetext

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import java.util.*

class BottomDialogFragment(

    private val readText: String

) : BottomSheetDialogFragment(), TextToSpeech.OnInitListener {

    private var binding: FragmentBottomDialogBinding? = null

    private val spf: SharedPreferences by lazy {
        requireContext().getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private val options: TranslatorOptions by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
    }

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
            binding.resultEditText.setText(readText)

            binding.listenButton.setOnClickListener {
                if(tts.isSpeaking) {
                    return@setOnClickListener
                }
                speakOut(binding.resultEditText.text.toString())
            }

            binding.translateButton.setOnClickListener {
                googleTranslator()
            }

        }
    }

    // TTS 실행
    private fun speakOut(speechText: String) {
        try {
            val ttsSpeed = spf.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_SPEECH_RATE)
            tts.setSpeechRate(ttsSpeed)
            tts.setPitch(TTS_PITCH)
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
                    binding?.let { binding ->
                        val speechText = binding.resultEditText.text.toString()
                        if(speechText.isNotBlank() && speechText.isNotEmpty()) {
                            speakOut(speechText)
                        }
                    }

                }
            } else {
                Log.e(TAG, "Initialization Failed!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun googleTranslator() {
        val englishKoreanTranslator = Translation.getClient(options)
        lifecycle.addObserver(englishKoreanTranslator)

        val isDownload = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false)
        Log.d(TAG, "다운로드 상태 : $isDownload")

        binding?.let { binding ->
            if(isDownload) {
                translate(binding, englishKoreanTranslator)
            } else {
                notReadyForTranslation()
            }
        }

    }

    // 번역 실행
    private fun translate(binding: FragmentBottomDialogBinding, englishKoreanTranslator: Translator) {
        val resultEditText = binding.resultEditText.text.toString()
        Log.e(TAG, resultEditText)

        englishKoreanTranslator.translate(resultEditText)
            .addOnSuccessListener { translatedText ->
                Log.d(TAG, "번역 결과 : $translatedText")
                binding.resultTranslationEditText.setText(translatedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    // 번역 준비안됨. 번역에 필요한 데이터 다운 후 다시 번역 시도
    private fun notReadyForTranslation() {
        Toast.makeText(requireContext(), resources.getString(R.string.translate_data_download), Toast.LENGTH_LONG).show()

        val modelManager = RemoteModelManager.getInstance()
        val koreanModel = TranslateRemoteModel.Builder(TranslateLanguage.KOREAN).build()

        val conditions = DownloadConditions.Builder()
            .requireCharging()
            .build()

        modelManager.download(koreanModel, conditions)
            .addOnSuccessListener {
                spf.edit().putBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, true).apply()
                googleTranslator()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), resources.getString(R.string.download_fail), Toast.LENGTH_SHORT).show()
                Log.e(TAG, it.toString())
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
        private const val TTS_PITCH = 0.8F
        private const val TTS_SPEECH_RATE = 0.8F
    }

}