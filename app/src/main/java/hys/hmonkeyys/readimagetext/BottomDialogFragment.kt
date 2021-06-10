package hys.hmonkeyys.readimagetext

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
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.mlkit.nl.translate.*
import hys.hmonkeyys.readimagetext.api.KakaoTranslateApi
import hys.hmonkeyys.readimagetext.databinding.FragmentBottomDialogBinding
import hys.hmonkeyys.readimagetext.model.TranslateKakaoModel
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

    private var translateCount = 1

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

            val replaceText = readText.replace("\n", " ")
            if(isAlmostUpperText()) {
                val result = replaceText.substring(0, 1).uppercase() + replaceText.substring(1).lowercase()
                binding.resultEditText.setText(result)
            } else {
                binding.resultEditText.setText(replaceText)
            }

            binding.listenButton.setOnClickListener {
                if(tts.isSpeaking) {
                    return@setOnClickListener
                }
                speakOut()
            }

            binding.translateButton.setOnClickListener {
                // 프로그레스바 돌고있으면 return
                if(binding.progressBar.isVisible) {
                    Toast.makeText(requireContext(), resources.getString(R.string.wait_please), Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                } else {
                    if(translateCount > 5) {
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

    private fun isAlmostUpperText() : Boolean {
        val onlyEnglishText = Regex("[^A-Za-z]").replace(readText, "")

        var textUpperCount = 0
        for(i in 0..onlyEnglishText.lastIndex) {
            if(onlyEnglishText[i].isUpperCase()) {
                textUpperCount += 1
            }
        }

        return if(onlyEnglishText.length - 3 < textUpperCount) {
            Log.d(TAG, "거의 대문자임")
            true
        } else {
            false
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
        val extractedResults = binding?.resultEditText?.text.toString()/*.replace("\n", " ")*/

        try {
            val ttsSpeed = spf.getFloat(SharedPreferencesConst.TTS_SPEED, TTS_SPEECH_RATE)
            tts.setSpeechRate(ttsSpeed)
            tts.setPitch(TTS_PITCH)
            tts.speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), resources.getString(R.string.tts_error), Toast.LENGTH_SHORT).show()
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
        tts.stop()
        tts.shutdown()
        translateCount = 0
        binding = null
        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_BottomDialogFragment"
        private const val TTS_PITCH = 0.8F
        private const val TTS_SPEECH_RATE = 0.8F
    }

}