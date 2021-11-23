package hys.hmonkeyys.readimagetext.screen.views.main.note

import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.viewModels
import androidx.core.view.isVisible
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.AndroidEntryPoint
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.databinding.ActivityNoteBinding
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.screen.BaseActivity
import hys.hmonkeyys.readimagetext.screen.views.main.note.adapter.NoteAdapter
import hys.hmonkeyys.readimagetext.utils.Constant
import hys.hmonkeyys.readimagetext.utils.Utility.isKorean
import hys.hmonkeyys.readimagetext.utils.Utility.toast
import java.util.*

@AndroidEntryPoint
internal class NoteActivity : BaseActivity<NoteViewModel, ActivityNoteBinding>(), TextToSpeech.OnInitListener {

    override val viewModel: NoteViewModel by viewModels<NoteViewModel>()
    override fun getViewBinding(): ActivityNoteBinding = ActivityNoteBinding.inflate(layoutInflater)

    private val tts: TextToSpeech? by lazy { TextToSpeech(this, this) }

    private lateinit var adapter: NoteAdapter

    /** 뷰 초기화 */
    override fun initViews() = with(binding) {
        // 뒤로가기 버튼
        backButton.setOnDuplicatePreventionClickListener { finish() }

        // 리사이클러뷰 초기화
        adapter = NoteAdapter(
            isLanguageKorean = isKorean(),
            onItemClick = {
                executeTTS(it)
            },
            onItemDeleteClick = {
                viewModel.deleteNote(it)
            }
        )
        recyclerView.adapter = adapter

        // TTS 초기화
        initTTS()

        // 하단 배너광고 초기화
        initAdmob()
    }

    /** TTS 실행 */
    private fun executeTTS(extractedResults: String) {
        tts?.let {
            if (it.isSpeaking) return

            try {
                it.setSpeechRate(viewModel.getTTSSpeed())
                it.setPitch(Constant.TTS_PITCH)
                it.speak(extractedResults, TextToSpeech.QUEUE_FLUSH, null, "id1")
            } catch (e: Exception) {
                e.printStackTrace()
                FirebaseCrashlytics.getInstance().recordException(e)
            }
        }
    }

    /** 한번 실행시켜 초기화 되도록 */
    private fun initTTS() {
        tts?.speak("" ,TextToSpeech.QUEUE_FLUSH, null, "id1")
    }

    /** 하단 배너광고 초기화 */
    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    Log.i(TAG, "광고 로드 성공 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                }
            }
        }
    }

    override fun observeData() {
        viewModel.noteStateData.observe(this) {
            when (it) {
                is NoteState.GetNoteData -> showNoteList(it.noteList)
            }
        }
    }

    /** noteList 비워져 있는지 여부 확인 후 보여주기 */
    private fun showNoteList(noteList: MutableList<Note>) {
        binding.noItemTextView.isVisible = noteList.isEmpty()
        adapter.setList(noteList)
    }

    override fun onPause() {
        super.onPause()

        // 실행중이면 정지
        tts?.let {
            if (it.isSpeaking) {
                it.stop()
            }
        }
    }

    override fun onDestroy() {
        // 정지 및 리소스 해제
        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }

    companion object {
        private const val TAG = "HYS_NoteActivity"
    }

    override fun onInit(status: Int) {
        val ttsLang = tts?.setLanguage(Locale.ENGLISH)

        if (status == TextToSpeech.SUCCESS) {
            if (ttsLang == TextToSpeech.LANG_MISSING_DATA || ttsLang == TextToSpeech.LANG_NOT_SUPPORTED) {
                toast(this, getString(R.string.not_support_language))
                return
            }
        } else {
            toast(this, getString(R.string.tts_init_fail))
        }
    }
}