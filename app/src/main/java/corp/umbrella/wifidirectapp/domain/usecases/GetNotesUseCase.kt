package corp.umbrella.wifidirectapp.domain.usecases

import androidx.lifecycle.LiveData
import corp.umbrella.wifidirectapp.domain.NotesRepository
import corp.umbrella.wifidirectapp.domain.entity.Note

class GetNotesUseCase(private val repository: NotesRepository) {

    operator fun invoke(): LiveData<List<Note>> {
        return repository.getNotes()
    }
}