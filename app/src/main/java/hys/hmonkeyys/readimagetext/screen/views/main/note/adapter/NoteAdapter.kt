package hys.hmonkeyys.readimagetext.screen.views.main.note.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.R
import hys.hmonkeyys.readimagetext.databinding.ItemNoteBinding
import hys.hmonkeyys.readimagetext.data.db.entity.Note
import hys.hmonkeyys.readimagetext.extensions.setOnDuplicatePreventionClickListener

class NoteAdapter(
    private val isLanguageKorean: Boolean,
    val onItemClick: (String) -> Unit,
    val onItemDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    private var noteList = mutableListOf<Note>()

    // 클릭에 따라 번역내용 On / Off
    var isShowKoreanText = false

    inner class ViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.resultTextView.text = note.english
            binding.resultTranslationTextView.text = note.korean

            // 삭제
            binding.deleteButton.setOnDuplicatePreventionClickListener {
                noteList.removeAt(layoutPosition)
                notifyItemRemoved(layoutPosition)

                onItemDeleteClick(note)
            }

            // 영어 언어 사용자는 듣기 안되게
            if (isLanguageKorean.not()) {
                binding.textViewListen.isGone = true
                binding.labelResultTranslation.isVisible = true
                binding.resultTranslationTextView.isVisible = true
            } else {
                // 듣기 버튼
                binding.textViewListen.setOnDuplicatePreventionClickListener {
                    onItemClick(binding.resultTextView.text.toString())
                }

                // 아이템 클릭 toggle
                binding.viewToggle.setOnDuplicatePreventionClickListener {
                    if (!isShowKoreanText) {
                        binding.labelResultTranslation.isVisible = true
                        binding.resultTranslationTextView.isVisible = true
                        binding.viewToggle.setBackgroundResource(R.drawable.ic_arrow_up)

                        isShowKoreanText = isShowKoreanText.not()
                    } else {
                        binding.labelResultTranslation.isGone = true
                        binding.resultTranslationTextView.isGone = true
                        binding.viewToggle.setBackgroundResource(R.drawable.ic_arrow_down)

                        isShowKoreanText = isShowKoreanText.not()
                    }

                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount(): Int = noteList.size

    @SuppressLint("NotifyDataSetChanged")
    fun setList(noteList: MutableList<Note>) {
        this.noteList = noteList
        notifyDataSetChanged()
    }

}