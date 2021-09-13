package hys.hmonkeyys.readimagetext.views.activity.note

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import hys.hmonkeyys.readimagetext.databinding.ActivityNoteBinding
import hys.hmonkeyys.readimagetext.db.entity.Note
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.views.BaseActivity
import hys.hmonkeyys.readimagetext.views.activity.main.MainActivity
import hys.hmonkeyys.readimagetext.views.activity.note.adapter.NoteAdapter
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class NoteActivity : BaseActivity<NoteViewModel>(

) {
    private val binding: ActivityNoteBinding by lazy { ActivityNoteBinding.inflate(layoutInflater) }
    override val viewModel: NoteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }

    override fun observeData() {
        viewModel.noteStateData.observe(this) {
            when (it) {
                is NoteState.Initialized -> {
                    initAdmob()
                    viewModel.getAllNote()
                }
                is NoteState.GetNoteData -> {
                    if (it.noteList.isEmpty()) {
                        binding.noItemTextView.visibility = View.VISIBLE
                    } else {
                        binding.noItemTextView.visibility = View.GONE
                        initRecyclerView(it.noteList)
                    }

                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.ttsStop()
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

    /** Recycler View 초기화 */
    private fun initRecyclerView(noteList: MutableList<Note>) {
        binding.backButton.setOnDuplicatePreventionClickListener {
            finish()
        }

        binding.recyclerView.adapter = NoteAdapter(noteList,
            onItemClick = {
                if (!viewModel.isSpeaking()) {
                    viewModel.speakOut(it)
                }

            },
            onItemDeleteClick = {
                viewModel.deleteNote(it)
            }
        )
    }

    companion object {
        private const val TAG = "HYS_NoteActivity"
    }
}