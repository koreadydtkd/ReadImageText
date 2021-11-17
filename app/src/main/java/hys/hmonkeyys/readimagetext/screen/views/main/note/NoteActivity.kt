package hys.hmonkeyys.readimagetext.screen.views.main.note

import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import hys.hmonkeyys.readimagetext.databinding.ActivityNoteBinding
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener
import org.koin.androidx.viewmodel.ext.android.viewModel
import hys.hmonkeyys.readimagetext.screen.BaseActivity
import hys.hmonkeyys.readimagetext.screen.views.main.note.adapter.NoteAdapter
import hys.hmonkeyys.readimagetext.utils.Utility.isKorean

internal class NoteActivity : BaseActivity<NoteViewModel, ActivityNoteBinding>() {

    override val viewModel: NoteViewModel by viewModel()
    override fun getViewBinding(): ActivityNoteBinding = ActivityNoteBinding.inflate(layoutInflater)

    private lateinit var adapter: NoteAdapter

    /** 뷰 초기화 */
    override fun initViews() = with(binding) {
        // 뒤로가기 버튼
        backButton.setOnDuplicatePreventionClickListener { finish() }

        adapter = NoteAdapter(
            isLanguageKorean = isKorean(),
            onItemClick = {
                if (!viewModel.isSpeaking()) {
                    viewModel.speakOut(it)
                }
            },
            onItemDeleteClick = {
                viewModel.deleteNote(it)
            }
        )

        // 리사이클러뷰 초기화
        recyclerView.adapter = adapter

        // 하단 배너광고 초기화
        initAdmob()
    }

    /** 하단 배너광고 초기화 */
    private fun initAdmob() {
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()

        binding.adView.apply {
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.i(TAG, "광고 로드 성공 onAdLoaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    super.onAdFailedToLoad(error)
                    Log.e(TAG, "광고 로드 문제 발생 onAdFailedToLoad ${error.message}")
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
        if (noteList.isEmpty()) {
            binding.noItemTextView.visibility = View.VISIBLE
        } else {
            binding.noItemTextView.visibility = View.GONE
            adapter.setList(noteList)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.ttsStop()
    }

    companion object {
        private const val TAG = "HYS_NoteActivity"
    }
}