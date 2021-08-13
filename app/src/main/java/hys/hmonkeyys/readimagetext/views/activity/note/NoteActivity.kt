package hys.hmonkeyys.readimagetext.views.activity.note

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ActivityNoteBinding
import hys.hmonkeyys.readimagetext.model.entity.Note
import hys.hmonkeyys.readimagetext.utils.setOnDuplicatePreventionClickListener
import hys.hmonkeyys.readimagetext.views.BaseActivity
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
                    initStatusBar()
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

    private fun initStatusBar() {
        try {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.teal_200, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initRecyclerView(noteList: MutableList<Note>) {
        binding.backButton.setOnDuplicatePreventionClickListener {
            finish()
        }

        binding.recyclerView.adapter = NoteAdapter(noteList,
            onItemClick = {
                if (!viewModel.isSpeaking()) {
                    viewModel.speakOut(it)
                }

            }
        )
    }

    override fun onStop() {
        super.onStop()
        viewModel.ttsStop()
    }
}