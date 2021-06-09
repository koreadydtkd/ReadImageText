package hys.hmonkeyys.readimagetext

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.nl.translate.*
import hys.hmonkeyys.readimagetext.api.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.api.NaverTranslateApi
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.model.ResultTransferPapago
import hys.hmonkeyys.readimagetext.model.TranslateKakaoModel
import hys.hmonkeyys.readimagetext.utils.SharedPreferencesConst
import hys.hmonkeyys.readimagetext.utils.Util
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

    private var translateCount = 0

    /*// 영어 -> 일본어 번역
    private val englishToJapaneseOptions: TranslatorOptions by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.JAPANESE)
            .build()
    }

    // 일본어 -> 한국어 번역
    private val japaneseToKoreanOptions: TranslatorOptions by lazy {
        TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.JAPANESE)
            .setTargetLanguage(TranslateLanguage.KOREAN)
            .build()
    }*/

    private val tts: TextToSpeech by lazy {
        TextToSpeech(requireContext(), this)
    }

    /*private val mCountDown: CountDownTimer = object : CountDownTimer(60 * SECOND, SECOND) {
        override fun onTick(millisUntilFinished: Long) {
            if(isDownloaded()) {
                progressBarHide()
                Toast.makeText(requireContext(), "다운로드 완료", Toast.LENGTH_SHORT).show()
                cancel()
            }
        }
        override fun onFinish() {
            cancel()
            progressBarHide()
        }
    }*/

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
                // 프로그레스바 돌고있으면 return
                if(binding.progressBar.isVisible) {
                    Toast.makeText(requireContext(), resources.getString(R.string.wait_please), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    /*if(isDownloaded()) {
                        googleTranslatorEnglishToJapanese(binding.resultEditText.text.toString())
                        progressBarShow()
                    } else {
                        showDownloadDialog()
                    }*/

                    if(translateCount > 2) {
                        it.setBackgroundResource(R.drawable.clicked_background)
                        binding.translateTextView.setTextColor(Color.WHITE)
                        Toast.makeText(requireContext(), resources.getString(R.string.selected_translate_limit), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }

                    progressBarShow()
                    translateCount += 1
                    translateKakao(binding.resultEditText.text.toString())
                }


            }

        }
    }

    // TTS 초기화
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

    /*private fun isDownloaded(): Boolean {
        val isDownloadEN = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_EN, false)
        val isDownloadJP = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_JP, false)
        val isDownloadKR = spf.getBoolean(SharedPreferencesConst.IS_DOWNLOAD_TRANSLATOR_KR, false)
        Log.d(TAG, "다운로드 상태 : EN - $isDownloadEN / JP - $isDownloadJP / KR - $isDownloadKR")
        return isDownloadEN && isDownloadJP && isDownloadKR
    }*/

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

    // 영어 -> 일본어 번역 실행 (정확도를 위해서)
    /*private fun googleTranslatorEnglishToJapanese(resultEditText: String) {
        val englishToJapaneseTranslator = Translation.getClient(englishToJapaneseOptions)
        lifecycle.addObserver(englishToJapaneseTranslator)

        englishToJapaneseTranslator.translate(resultEditText)
            .addOnSuccessListener { translatedText ->
                japaneseToKoreanTranslate(translatedText)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }

    }*/

    // 일본어 -> 한국어 번역 실행 (정확도를 위해서)
    /*private fun japaneseToKoreanTranslate(translatedText: String) {
        val japaneseToKoreanTranslator = Translation.getClient(japaneseToKoreanOptions)
        lifecycle.addObserver(japaneseToKoreanTranslator)

        japaneseToKoreanTranslator.translate(translatedText)
            .addOnSuccessListener { translatedText ->
                Log.d(TAG, "번역 결과 : $translatedText")
                binding?.resultTranslationEditText?.setText(translatedText)
                progressBarHide()
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, exception.toString())
            }
    }*/

    /*private fun showDownloadDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("파일 다운로드")
            .setMessage("번역에 필요한 파일이 다운로드되지 않았습니다.")
            .setCancelable(false)
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("다운로드") { _, _ ->
                progressBarShow()
                mCountDown.start()
                Util(requireContext()).downloadGoogleTranslator()
                Toast.makeText(requireContext(), "번역에 필요한 파일을 다운받습니다.\n잠시만 기다려주세요.", Toast.LENGTH_LONG).show()
            }
            .show()
    }*/

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

        KakaoTranslateApi.create().translateKakao(replaceText, "en", "kr").enqueue(object : Callback<TranslateKakaoModel> {
            override fun onResponse(call: Call<TranslateKakaoModel>, response: Response<TranslateKakaoModel>) {
                if(response.isSuccessful.not()) {
                    Log.e(TAG, "KAKAO translate Fail")
                    Toast.makeText(requireContext(), resources.getString(R.string.translate_fail), Toast.LENGTH_SHORT).show()
                    return
                }

                response.body()?.let { translateKakaoModel ->
                    val items = translateKakaoModel.translatedText?.get(0)

                    val sb: StringBuilder = StringBuilder()
                    items?.forEach {
                        sb.append(it)
                    }
                    binding?.resultTranslationEditText?.setText(sb.toString())
                    progressBarHide()
                }
            }

            override fun onFailure(call: Call<TranslateKakaoModel>, t: Throwable) {
                Log.e(TAG, t.message.toString())
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
        tts?.let {
            tts.stop()
            tts.shutdown()
        }
//        mCountDown.cancel()
        translateCount = 0
        binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_BottomDialogFragment"
        private const val TTS_PITCH = 0.8F
        private const val TTS_SPEECH_RATE = 0.8F

//        private const val SECOND = 1000L
    }

}