package hys.hmonkeyys.readimagetext

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.*
import hys.hmonkeyys.readimagetext.api.NaverApi
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.model.ResultTransferPapago
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class BottomDialogFragment(

    private val readText: String

) : BottomSheetDialogFragment(), TextToSpeech.OnInitListener {

    private var binding: FragmentBottomDialogBinding? = null

    private val spf: SharedPreferences by lazy {
        requireContext().getSharedPreferences(SharedPreferencesConst.APP_DEFAULT_KEY, Context.MODE_PRIVATE)
    }

    private val englishToJapaneseOptions: TranslatorOptions by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.JAPANESE)
            .build()
    }

    private val japaneseToKoreanOptions: TranslatorOptions by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.JAPANESE)
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
                val speakText = binding.resultEditText.text.toString().replace("\n", " ")
                speakOut(speakText)
            }

            binding.translateButton.setOnClickListener {
                if(isDownloaded()) {
                    googleTranslatorEnglishToJapanese(binding.resultEditText.text.toString())
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    showDialog()
                }

//                translatePapago(binding.resultEditText.text.toString())
            }

        }
    }

    private fun isDownloaded(): Boolean {
        val isDownloadEN = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, false)
        val isDownloadJP = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, false)
        val isDownloadKR = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false)
        Log.d(TAG, "다운로드 상태 : EN - $isDownloadEN, JP - $isDownloadJP, KR - $$isDownloadKR")
        return isDownloadEN && isDownloadJP && isDownloadKR
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
                        val speechText = binding.resultEditText.text.toString().replace("\n", " ")
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

    // 영어 -> 일본어 번역 실행 (정확도를 위해서)
    private fun googleTranslatorEnglishToJapanese(resultEditText: String) {
        val englishToJapaneseTranslator = Translation.getClient(englishToJapaneseOptions)
        lifecycle.addObserver(englishToJapaneseTranslator)

        englishToJapaneseTranslator.translate(resultEditText)
            .addOnSuccessListener { translatedText ->
                japaneseToKoreanTranslate(translatedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }

    }

    // 일본어 -> 한국어 번역 실행 (정확도를 위해서)
    private fun japaneseToKoreanTranslate(translatedText: String) {
        val japaneseToKoreanTranslator = Translation.getClient(japaneseToKoreanOptions)
        lifecycle.addObserver(japaneseToKoreanTranslator)

        japaneseToKoreanTranslator.translate(translatedText)
            .addOnSuccessListener { translatedText ->
                Log.d(TAG, "번역 결과 : $translatedText")
                binding?.resultTranslationEditText?.setText(translatedText)
                binding?.progressBar?.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }

    private fun showDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("파일 다운로드")
            .setMessage("번역에 필요한 파일이 다운로드되지 않았습니다.\n잠시 후 다시 시도해주세요.")
            .setCancelable(false)
            .setPositiveButton("확인") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun translatePapago(translateText: String) {
        val replaceText = translateText.replace("\n", " ")

        NaverApi.create().transferPapago("en", "ko", replaceText).enqueue(object : Callback<ResultTransferPapago> {
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