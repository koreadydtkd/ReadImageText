package hys.hmonkeyys.readimagetext.views.activity.note

import android.os.Bundle
import android.view.View
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

    override fun onStop() {
        super.onStop()
        viewModel.ttsStop()
    }
}