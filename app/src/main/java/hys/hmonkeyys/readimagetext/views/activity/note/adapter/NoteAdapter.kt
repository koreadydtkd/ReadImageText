package hys.hmonkeyys.readimagetext.views.activity.note.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.databinding.ItemNoteBinding
import hys.hmonkeyys.readimagetext.db.entity.Note
import hys.hmonkeyys.readimagetext.utils.Expansion.setOnDuplicatePreventionClickListener

class NoteAdapter(
    private val noteList: MutableList<Note>,
    val onItemClick: (String) -> Unit,
    val onItemDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    // 클릭에 따라 번역내용 On / Off
    var isShowKoreanText = false

    inner class ViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.resultTextView.text = note.english
            binding.resultTranslationTextView.text = note.korean

            // 듣기 버튼
            binding.listenButton.setOnDuplicatePreventionClickListener {
                onItemClick(binding.resultTextView.text.toString())
            }

            // 듣기 삭제
            binding.deleteButton.setOnDuplicatePreventionClickListener {
                noteList.removeAt(layoutPosition)
                notifyItemRemoved(layoutPosition)

                onItemDeleteClick(note)
            }

            // 아이템 클릭
            binding.root.setOnDuplicatePreventionClickListener {
                if (!isShowKoreanText) {
                    binding.textView2.isVisible = true
                    binding.resultTranslationTextView.isVisible = true

                    isShowKoreanText = isShowKoreanText.not()
                } else {
                    binding.textView2.isVisible = false
                    binding.resultTranslationTextView.isVisible = false

                    isShowKoreanText = isShowKoreanText.not()
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(noteList[position])
    }

    override fun getItemCount(): Int = noteList.size
}