package corp.umbrella.wifidirectapp.data

import androidx.lifecycle.LiveData
import corp.umbrella.wifidirectapp.data.database.NotesDao
import corp.umbrella.wifidirectapp.domain.NotesRepository
import corp.umbrella.wifidirectapp.domain.entity.Note

class NotesRepositoryImpl(private val dao: NotesDao) : NotesRepository {

    override suspend fun saveNote(note: Note) {
        dao.insertNote(note)
    }

    override suspend fun deleteNotes() {
        dao.deleteNotes()
    }

    override fun getNotes(): LiveData<List<Note>> {
        return dao.getNotes()
    }
}