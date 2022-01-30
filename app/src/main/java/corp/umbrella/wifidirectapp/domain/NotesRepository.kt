package corp.umbrella.wifidirectapp.domain

import androidx.lifecycle.LiveData
import corp.umbrella.wifidirectapp.domain.entity.Note

interface NotesRepository {
    suspend fun saveNote(note: Note)
    suspend fun deleteNotes()
    fun getNotes(): LiveData<List<Note>>
}