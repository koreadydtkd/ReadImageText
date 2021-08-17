package hys.hmonkeyys.readimagetext.views.activity.note.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import hys.hmonkeyys.readimagetext.databinding.ItemNoteBinding
import hys.hmonkeyys.readimagetext.model.entity.Note
import hys.hmonkeyys.readimagetext.utils.setOnDuplicatePreventionClickListener

class NoteAdapter(
    private val noteList: MutableList<Note>,
    val onItemClick: (String) -> Unit,
    val onItemDeleteClick: (Note) -> Unit
) : RecyclerView.Adapter<NoteAdapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.resultTextView.text = note.english
            binding.resultTranslationTextView.text = note.korean

            binding.listenButton.setOnDuplicatePreventionClickListener {
                onItemClick(binding.resultTextView.text.toString())
            }

            binding.deleteButton.setOnDuplicatePreventionClickListener {
                noteList.removeAt(layoutPosition)
                notifyItemRemoved(layoutPosition)

                onItemDeleteClick(note)
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