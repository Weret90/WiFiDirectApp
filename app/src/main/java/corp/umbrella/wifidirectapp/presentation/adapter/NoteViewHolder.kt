package corp.umbrella.wifidirectapp.presentation.adapter

import androidx.recyclerview.widget.RecyclerView
import corp.umbrella.wifidirectapp.databinding.ItemNoteBinding
import corp.umbrella.wifidirectapp.domain.entity.Note

class NoteViewHolder(private val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(note: Note) {
        binding.message.text = note.fromWho + note.text
    }
}